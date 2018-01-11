package org.pearson.kafka_spring.controller;

import java.util.ArrayList;
import java.util.List;

import org.pearson.kafka_spring.model.KafkaConsumerMain;
import org.pearson.kafka_spring.model.Pod;
import org.pearson.kafka_spring.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class dataController {
	
	@Autowired
	private DataRepository dataRepository;
	
	@RequestMapping(value = "/greeting",method = RequestMethod.GET)
    @ResponseBody
    public String greeting() {
		List<Pod> list=new ArrayList<>();
		
		KafkaConsumerMain consumerMain=new KafkaConsumerMain(list);
		try {
			consumerMain.runConsumer();
			
			consumerMain.runConsumerMetric();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
//		System.out.println("List Size: "+list.size());
//		
		for (Pod pod:list){
			dataRepository.save(pod);
			
		}
		

        return "Sample";
    }


}
