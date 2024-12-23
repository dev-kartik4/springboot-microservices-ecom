package com.shoppix.product_reactive_service.repo;

import com.shoppix.product_reactive_service.entity.MerchantDetails;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.shoppix.product_reactive_service.entity.Product;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepo extends ReactiveMongoRepository<Product, String> {

    @Query("{productName: ?0}")
    Mono<Product> findProductByProductName(String productName);

    @Query("{merchantId: ?0, parentProductId: ?0}")
    Mono<MerchantDetails> findMerchantDetailsByParentProductId(String merchantId, String parentProductId);
}
