package com.meru.order_service.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.meru.order_service.bean.Order;
import com.meru.order_service.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {
	
	@Autowired
	public OrderService orderService;
	
	@PostMapping("/addOrder")
	@ResponseBody
	public ResponseEntity<Order> saveOrder(@RequestBody Order order){
		
		
		System.out.println("ORDER CREATED FOR CUSTOMER");
		
		Order order1 = orderService.saveOrder(order);
		
		return new ResponseEntity<Order>(order1,HttpStatus.OK);
	}
	
	@GetMapping("/getOrder/{orderId}")
	public ResponseEntity<Order> getOrderById(@PathVariable("orderId") int orderId){
		
		Order order = orderService.getOrderById(orderId).get();
		
		if(order==null) 
			return new ResponseEntity<Order>(order,HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<Order>(order,HttpStatus.OK);
	}

	@GetMapping("/getAllOrders/{customerId}")
	public List<Order> getAllOrdersByCustomerId(@PathVariable("customerId") int customerId){

		return orderService.getAllOrdersByCustomerId(customerId);
	}

	@DeleteMapping("/delete/{orderId}")
	public ResponseEntity<String> deleteByOrderId(@PathVariable("orderId") int orderId) {
		
		orderService.deleteByOrderId(orderId);
		
		return new ResponseEntity("DELETE SUCCESSFULL",HttpStatus.OK);
	}

}
