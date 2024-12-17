package com.shoppix.product_reactive_service.repo;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.shoppix.product_reactive_service.entity.Product;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepo extends ReactiveMongoRepository<Product, Integer> {

    @Query("{productId:  ?0}")
    Mono<Product> findById(int productId);

    @Query("{productId:  ?0}")
    Mono<Boolean> deleteByProductId(int productId);

    @Query("{productName: ?0}")
    Mono<Product> findProductByProductName(String productName);

}
