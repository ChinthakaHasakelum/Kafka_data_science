package org.pearson.kafka_spring.repository;

import org.pearson.kafka_spring.model.Pod;
import org.springframework.data.cassandra.repository.CassandraRepository;



public interface DataRepository extends CassandraRepository<Pod> {

	
}
