package com.shoppix.product_reactive_service.repo;

import com.shoppix.product_reactive_service.entity.SKUId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SKURepo extends ReactiveMongoRepository<SKUId, String> {
    // You can define custom query methods if needed
}

