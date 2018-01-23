package org.pearson.kafka_spring.repository;

import java.util.List;

import org.pearson.kafka_spring.model.Pod;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;



public interface DataRepository extends CassandraRepository<Pod> {

	
	@Query("SELECT * FROM Pod")
    List<Pod> getAll();
}
