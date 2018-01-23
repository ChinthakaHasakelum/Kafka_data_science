package org.pearson.kafka_spring.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;
import org.pearson.kafka_spring.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class KafkaConsumerMain {

	private final static String TOPIC = "test";
	private final static String TOPICMETRIC = "test1";
	private final static String BOOTSTRAP_SERVERS = "18.217.127.57:9092";

	private List<Pod> list;

	@Autowired
	private DataRepository dataRepository;

	public KafkaConsumerMain(List<Pod> list) {
		this.list = list;
	}

	public Consumer<Long, String> createConsumer(String topic) {
		final Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer" + System.currentTimeMillis());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		// Create the consumer using props.
		final Consumer<Long, String> consumer = new KafkaConsumer<Long, String>(props);
		// Subscribe to the topic.
		consumer.subscribe(Collections.singletonList(topic));
		return consumer;
	}

	public void runConsumer() throws InterruptedException {

		final Consumer<Long, String> consumer = createConsumer(TOPIC);
		final int giveUp = 100;
		int noRecordsCount = 0;
		while (true) {
			final ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);
			if (consumerRecords.count() == 0) {
				noRecordsCount++;
				if (noRecordsCount > giveUp)
					break;
				else
					continue;
			}

			// KafkaProducerMain.runProducer1(3, consumerRecords);

			System.out.println(consumerRecords.count());

			consumerRecords.forEach(record -> {
				// System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
				// record.key(), record.value(),
				// record.partition(), record.offset());

				Pod pod = new Pod();

				JSONObject jsonrecord = new JSONObject(record.value());
				JSONObject eventValue = new JSONObject(jsonrecord.getString("EventValue"));
				JSONObject metadata = eventValue.getJSONObject("metadata");

				String metaString = metadata.toString();
				String reason = eventValue.getString("reason");
				String message = eventValue.getString("message");
				String firstTimeStamp = eventValue.getString("firstTimestamp");
				String lastTimeStamp = eventValue.getString("lastTimestamp");

				System.out.println("MEtadata: " + metaString);
				pod.setUid(metadata.getString("uid"));
				pod.setPodName(metadata.getString("name"));
				pod.setNamespace(metadata.getString("namespace"));
				pod.setReason(reason);
				pod.setCreationTimestamp(firstTimeStamp);
				pod.setLastTimestamp(lastTimeStamp);

				dataRepository.save(pod);
				list.add(pod);

				System.out.println("EventValue: " + eventValue.toString());

			});

			consumer.commitAsync();

		}
		consumer.close();
		System.out.println("DONE");

	}

	public void runConsumerMetric() throws InterruptedException {
		// ObjectMapper mapper = new ObjectMapper();
		final Consumer<Long, String> consumer = createConsumer(TOPICMETRIC);
		final int giveUp = 100;
		int noRecordsCount = 0;
		while (true) {
			final ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);
			if (consumerRecords.count() == 0) {
				noRecordsCount++;
				if (noRecordsCount > giveUp)
					break;
				else
					continue;
			}

			consumerRecords.forEach(record -> {

				JSONObject jsonMetric = new JSONObject(record.value());
				String MetricsName = jsonMetric.getString("MetricsName");

				if (MetricsName.contains("cpu/usage") == true || MetricsName.contains("cpu/limit") == true
						|| MetricsName.contains("memory/usage") == true
						|| MetricsName.contains("memory/limit") == true) {

					JSONObject metricTags = jsonMetric.getJSONObject("MetricsTags");
					JSONObject metricValue = jsonMetric.getJSONObject("MetricsValue");
					String podName = "";
					try {
						podName = metricTags.getString("pod_name");
					} catch (Exception e) {
						// TODO: handle exception
					}

					// System.out.println("sdfsdfsdfsdf"+metricValue.toString());
					// System.out.println("GETSTRINFDGDFGf"+metricValue.get("value"));

					System.out.println(" " + jsonMetric.toString());
					Pod pod = null;
					for (int i = 0; i < list.size(); i++) {

						pod = list.get(i);
						if (pod.getPodName().contains(podName)) {

							if (MetricsName.contains("cpu/usage") == true) {

								pod.setCpuUsage(metricValue.get("value").toString());

							} else if (MetricsName.contains("cpu/limit") == true) {
								pod.setCpuLimit(metricValue.get("value").toString());
							} else if (MetricsName.contains("memory/usage") == true) {
								pod.setMemoryUsage(metricValue.get("value").toString());
							} else if (MetricsName.contains("memory/limit") == true) {
								pod.setMemoryLimit(metricValue.get("value").toString());
							}

						}

					}

					list.add(pod);
				}

			});
			consumer.commitAsync();

		}
		consumer.close();
		System.out.println("DONE");

	}

	public void runConsumerBoth() throws InterruptedException {

		final Consumer<Long, String> consumer = createConsumer(TOPIC);
		final Consumer<Long, String> consumermetric = createConsumer(TOPICMETRIC);
		final int giveUp = 100;
		int noRecordsCount = 0;
		while (true) {
			final ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);
			final ConsumerRecords<Long, String> consumerRecordsMetric = consumermetric.poll(1000);
			if (consumerRecords.count() == 0) {
				noRecordsCount++;
				if (noRecordsCount > giveUp)
					break;
				else
					continue;
			}

			// KafkaProducerMain.runProducer1(3, consumerRecords);

			System.out.println(consumerRecords.count());
			
			

			consumerRecords.forEach(record -> {
				// System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
				// record.key(), record.value(),
				// record.partition(), record.offset());

				Pod pod = new Pod();

				JSONObject jsonrecord = new JSONObject(record.value());
				JSONObject eventValue = new JSONObject(jsonrecord.getString("EventValue"));
				JSONObject metadata = eventValue.getJSONObject("metadata");

				String metaString = metadata.toString();
				String reason = eventValue.getString("reason");
				String message = eventValue.getString("message");
				String firstTimeStamp = eventValue.getString("firstTimestamp");
				String lastTimeStamp = eventValue.getString("lastTimestamp");

				//System.out.println("MEtadata: " + metaString);
				pod.setUid(metadata.getString("uid"));
				pod.setPodName(metadata.getString("name"));
				pod.setNamespace(metadata.getString("namespace"));
				pod.setReason(reason);
				pod.setCreationTimestamp(firstTimeStamp);
				pod.setLastTimestamp(lastTimeStamp);

				//dataRepository.save(pod);
				list.add(pod);

				System.out.println("EventValue: " + eventValue.toString());

			});

			consumerRecordsMetric.forEach(record -> {

				JSONObject jsonMetric = new JSONObject(record.value());
				String MetricsName = jsonMetric.getString("MetricsName");

				if (MetricsName.contains("cpu/usage") == true || MetricsName.contains("cpu/limit") == true
						|| MetricsName.contains("memory/usage") == true
						|| MetricsName.contains("memory/limit") == true) {

					JSONObject metricTags = jsonMetric.getJSONObject("MetricsTags");
					JSONObject metricValue = jsonMetric.getJSONObject("MetricsValue");
					String podName = "";
					try {
						podName = metricTags.getString("pod_name");
					} catch (Exception e) {
						// TODO: handle exception
					}

					System.out.println("Metric Name: " + jsonMetric.toString());
					Pod pod = null;
					for (int i = 0; i < list.size(); i++) {

						pod = list.get(i);
						if (pod.getPodName().contains(podName) && !podName.isEmpty()) {
							
							System.out.println("PODNAME: "+pod.getPodName() +" Equals: "+podName);

							if (MetricsName.contains("cpu/usage") == true) {

								pod.setCpuUsage(metricValue.get("value").toString());

							} else if (MetricsName.contains("cpu/limit") == true) {
								pod.setCpuLimit(metricValue.get("value").toString());
							} else if (MetricsName.contains("memory/usage") == true) {
								pod.setMemoryUsage(metricValue.get("value").toString());
							} else if (MetricsName.contains("memory/limit") == true) {
								pod.setMemoryLimit(metricValue.get("value").toString());
							}

						}

					}

					list.add(pod);
				}

			});
			
			consumer.commitAsync();
			consumermetric.commitAsync();

		}
		consumer.close();
		consumermetric.close();
		System.out.println("DONE BOTH");

	}

}
