package com.shoppix.order_reactive_service.repo;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.shoppix.order_reactive_service.bean.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepo extends ReactiveMongoRepository<Order, Long> {

    @Query("{customerId: ?0}")
    Flux<Order> findAllByCustomerId(int customerId);

    @Query("{orderId: ?0}")
    Mono<Order> findById(long orderId);

    @Query("{productId: ?0}")
    Mono<Order> findOrderDetailsByProductId(int productId);

    @Query("{orderSerialKey: ?0}")
    Mono<Order> findByOrderSerialKey(String orderSerialKey);

    @Query("{orderSerialKey:  ?0}")
    Mono<Boolean> deleteByOrderSerialKey(String orderSerialKey);
}
