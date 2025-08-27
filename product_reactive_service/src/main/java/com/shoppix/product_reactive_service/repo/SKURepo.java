package com.shoppix.product_reactive_service.repo;

import com.shoppix.product_reactive_service.entity.SKU;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SKURepo extends ReactiveMongoRepository<SKU, String> {
    // You can define custom query methods if needed
}

