package com.meru.order_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meru.order_service.bean.Order;
import com.meru.order_service.repo.OrderRepo;

@Service
public class OrderService {
	
	@Autowired
	public OrderRepo orderRepo;
	
	public Order saveOrder(Order order) {

		Optional<Order> order1 = orderRepo.findById(order.getOrderId());

		if(order1==null) {
			Order ord = new Order();
			ord.setOrderId(order.getOrderId());
			ord.setAddressId(order.getAddressId());
			ord.setCustomerId(order.getCustomerId());
			ord.setProductId(order.getProductId());
			ord.setOrderDate(order.getOrderDate());
			ord.setStatus(order.getStatus());

			return ord;
		}else {
			order = orderRepo.save(order);
		}
		return order;
	}
	
	public Optional<Order> getOrderById(int orderId) {
		return orderRepo.findById(orderId);
	}

	public List<Order> getAllOrders(){
		return orderRepo.findAll();
	}

	public void deleteByOrderId(int orderId) {
		orderRepo.deleteById(orderId);
	}

	public List<Order> getAllOrdersByCustomerId(int customerId) {

		List<Order> orders = new ArrayList<>();
		orderRepo.findAllByCustomerId(customerId).forEach(orders::add);

		return orders;
	}
}
