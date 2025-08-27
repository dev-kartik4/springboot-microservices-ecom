package com.shoppix.product_reactive_service.repo;

import com.shoppix.product_reactive_service.pojo.Sequence;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SequenceRepo extends ReactiveMongoRepository<Sequence, String> {
}

