package com.shoppix.merchant_service_reactive.repo;

import com.shoppix.merchant_service_reactive.entity.MerchantDetails;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MerchantRepo extends ReactiveMongoRepository<MerchantDetails,Long> {

    @Query("{'emailId':  ?0}")
    Mono<MerchantDetails> findByEmailId(String emailId);

    @Query("{'merchantSellingName':  ?0}")
    Mono<MerchantDetails> findByMerchantSellerName(String merchantSellingName);
}
