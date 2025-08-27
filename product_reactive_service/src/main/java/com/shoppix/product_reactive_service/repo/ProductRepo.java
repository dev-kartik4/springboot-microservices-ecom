package com.shoppix.product_reactive_service.repo;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.shoppix.product_reactive_service.entity.Product;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepo extends ReactiveMongoRepository<Product, String> {

    @Query("{'parentProductId': ?0}")
    Mono<Product> findByParentProductId(String parentProductId);

    @Query("{'productName': ?0}")
    Mono<Product> findProductByProductName(String productName);

    @Query("{'parentProductId': ?0}")
    Mono<Void> deleteByParentProductId(String parentProductId);
}
