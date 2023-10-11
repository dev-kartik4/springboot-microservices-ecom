package com.meru.order_service.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.meru.order_service.bean.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends MongoRepository<Order, Integer> {

    @Query("{customerId: ?0}")
    List<Order> findAllByCustomerId(int customerId);

    Optional<Order> findById(int orderId);

    @Query("{productId: ?0}")
    Order findOrderDetailsByProductId(int productId);

    @Query("{orderSerialKey: ?0}")
    Order findByOrderSerialKey(String orderSerialKey);

    @Query("{orderSerialKey:  ?0}")
    boolean deleteByOrderSerialKey(String orderSerialKey);
}
