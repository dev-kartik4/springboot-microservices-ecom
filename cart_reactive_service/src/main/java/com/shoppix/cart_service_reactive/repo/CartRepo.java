package com.shoppix.cart_service_reactive.repo;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.shoppix.cart_service_reactive.entity.Cart;
import reactor.core.publisher.Mono;

@Repository
public interface CartRepo extends ReactiveMongoRepository<Cart, Integer> {

    @Query("{'customerIdForCart':  ?0}")
    Mono<Cart> findByCustomerIdForCart(int customerIdForCart);


}
