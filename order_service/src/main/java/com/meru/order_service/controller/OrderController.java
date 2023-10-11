package com.meru.order_service.controller;

import java.util.List;
import java.util.Optional;

import com.meru.order_service.bean.OrderRequest;
import com.meru.order_service.exception.OrderServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.meru.order_service.bean.Order;
import com.meru.order_service.service.OrderService;

@CrossOrigin("*")
@RestController
@RequestMapping("/orders")
public class OrderController {
	
	@Autowired
	public OrderService orderService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

	private static final String ORDER_SERVICE = "orderService";

	/**
	 *
	 * @param orderRequest
	 * @return
	 * @throws OrderServiceException
	 */
	@PostMapping("/placeOrder")
	@CircuitBreaker(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@Retry(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@RateLimiter(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@ResponseBody
	public ResponseEntity<Order> saveOrder(@RequestBody OrderRequest orderRequest) throws OrderServiceException {

		Optional<Order> orderData = Optional.ofNullable(orderService.getOrderDetailsByProductId(orderRequest.getProductId()));

		if(!orderData.isPresent()){
			ResponseEntity<Order> order = orderService.saveOrderRequest(orderRequest);
			return new ResponseEntity(order,HttpStatus.OK);
		}else {
			throw new OrderServiceException("ORDER ALREADY EXISTS FOR THIS PRODUCT [" + orderRequest.getProductId() + "]");
		}
	}
	
	@GetMapping("/getOrder/{orderSerialKey}")
	@CircuitBreaker(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@Retry(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@RateLimiter(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	public ResponseEntity<Order> getOrderByOrderSerialKey(@PathVariable("orderSerialKey") String orderSerialKey) throws OrderServiceException{
		
		Order order = orderService.getOrderByOrderSerialKey(orderSerialKey).get();
		
		if(order!=null)
			return new ResponseEntity<Order>(order,HttpStatus.OK);
		else {
			throw new OrderServiceException("ORDER DETAILS NOT GENERATED FOR THIS SERIALKEY [" + orderSerialKey + "]");
		}

	}

	@GetMapping("/getAllOrders/{customerId}")
	@CircuitBreaker(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@Retry(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@RateLimiter(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	public List<Order> getAllOrdersByCustomerId(@PathVariable("customerId") int customerId) throws OrderServiceException{

		return orderService.getAllOrdersByCustomerId(customerId);
	}


	@GetMapping("/getOrderDetails/{productId}")
	@CircuitBreaker(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@Retry(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@RateLimiter(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	public ResponseEntity<Order> getOrderDetailsByProductId(@PathVariable("productId") int productId) throws OrderServiceException{

		Optional<Order> orderDetails = Optional.ofNullable(orderService.getOrderDetailsByProductId(productId));
		if(orderDetails.isPresent()){
			LOGGER.info("FETCHING ORDER DETAILS BY PRODUCT ID ["+ productId+"]");
			return new ResponseEntity(orderDetails,HttpStatus.OK);
		}else {
			throw new OrderServiceException("NO ORDER TOOK PLACE FOR THIS PRODUCT ID [" + productId + "]");
		}
	}

	@DeleteMapping("/delete/{orderSerialKey}")
	@CircuitBreaker(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@Retry(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@RateLimiter(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	public ResponseEntity<String> deleteByOrderSerialKey(@PathVariable("orderSerialKey") String orderSerialKey) {

		if(orderService.deleteByOrderSerialKey(orderSerialKey)){
			LOGGER.info("ORDER WITH SERIAL KEY ["+orderSerialKey+"] DELETED SUCCESSFULLY");
			return new ResponseEntity("ORDER WITH SERIAL KEY ["+orderSerialKey+"] DELETED SUCCESSFULLY",HttpStatus.OK);
		}else{
			throw new OrderServiceException("ERROR DELETING ORDER WITH SERIAL KEY ["+orderSerialKey+"] NOT FOUND");
		}
	}

	public ResponseEntity<?> orderFallback(Exception e){
		return new ResponseEntity("ERROR, WE ARE UNABLE TO COMMUNICATE WITH ORDER SERVICE",HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
