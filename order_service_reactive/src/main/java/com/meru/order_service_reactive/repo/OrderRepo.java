package com.meru.order_service_reactive.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.meru.order_service_reactive.bean.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

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
