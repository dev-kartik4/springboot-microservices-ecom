package com.meru.order_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.meru.order_service.bean.Order;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Integer>{

    List<Order> findAllByCustomerId(int customerId);
}
