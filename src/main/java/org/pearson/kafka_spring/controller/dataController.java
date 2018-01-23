package org.pearson.kafka_spring.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.pearson.kafka_spring.model.KafkaConsumerMain;
import org.pearson.kafka_spring.model.Pod;
import org.pearson.kafka_spring.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class dataController {
	
	@Autowired
	private DataRepository dataRepository;
	
	@RequestMapping(value = "/kafka",method = RequestMethod.GET)
    @ResponseBody
    public String greeting() {
		 List<Pod> list=new ArrayList<>();
		 Runnable runnable=new Runnable() {
				
				@Override
				public void run() {
					System.out.println("Hellloooooo");
					if(list.size()>0) {
						for (Pod pod:list){
							dataRepository.save(pod);
						}
						list.clear();
						
					}
					
				}
			};
		 
		  ScheduledExecutorService service = Executors
	                .newSingleThreadScheduledExecutor();
	service.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.MINUTES);
		
		KafkaConsumerMain consumerMain=new KafkaConsumerMain(list);
		try {
			//consumerMain.runConsumer();
			consumerMain.runConsumerBoth();
			//consumerMain.runConsumerMetric();
		} catch (InterruptedException e) {
	
			e.printStackTrace();
		}
	
        return "Sample";
    }
	
	@RequestMapping(value = "/pod", method = RequestMethod.GET, produces = "application/json")

	public @ResponseBody List<Pod> getPodDetails() {
		List<Pod> list =new ArrayList<Pod>();
		list=dataRepository.getAll();
		
		for(Pod pod:list) {
			System.out.println(pod.getPodName());
		}
		return list;
	}


}
