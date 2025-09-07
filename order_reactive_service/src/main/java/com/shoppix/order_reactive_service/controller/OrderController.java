package com.shoppix.order_reactive_service.controller;

import com.shoppix.order_reactive_service.bean.OrderRequest;
import com.shoppix.order_reactive_service.exception.OrderServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shoppix.order_reactive_service.bean.Order;
import com.shoppix.order_reactive_service.service.OrderService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {

	@Autowired
	public OrderService orderService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

	private static final String ORDER_SERVICE = "orderService";
	public static final String ORDER_SERVICE_URL = "http://order-reactive-service/api/v1/orders";
	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * API TO CREATE OR SAVE NEW ORDER
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
	public ResponseEntity<Mono<Order>> saveOrder(@RequestBody OrderRequest orderRequest) throws OrderServiceException {

		LOGGER.info("CREATING ORDER REQUEST");
		Mono<Order> orderData = orderService.saveOrderRequest(orderRequest);

		return new ResponseEntity(orderData, HttpStatus.OK);
	}

	/**
	 * WILL BE CONTROLLED BY ADMIN ONLY
	 * <p>
	 * API TO FETCH ORDER BY ORDER SERIAL KEY
	 *
	 * @param orderSerialKey
	 * @return
	 * @throws OrderServiceException
	 */

	@GetMapping("/getOrder/{orderSerialKey}")
	@CircuitBreaker(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@Retry(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@RateLimiter(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	public ResponseEntity<Order> getOrderByOrderSerialKey(@PathVariable("orderSerialKey") String orderSerialKey) throws OrderServiceException{

		LOGGER.info("FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+orderSerialKey+"]");
		Mono<Order> orderData = orderService.getOrderByOrderSerialKey(orderSerialKey);

		return new ResponseEntity(orderData, HttpStatus.OK);
	}

	/**
	 * WILL BE CONTROLLED BY ADMIN ONLY
	 * <p>
	 * API TO FETCH ALL ORDERS BY CUSTOMER ID
	 *
	 * @param customerId
	 * @return
	 * @throws OrderServiceException
	 */
	@GetMapping("/getAllOrders/{customerId}")
	@CircuitBreaker(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@Retry(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@RateLimiter(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	public ResponseEntity<Flux<Order>> getAllOrdersByCustomerId(@PathVariable("customerId") int customerId) throws OrderServiceException{

		LOGGER.info("FETCHING ALL ORDERS OF CUSTOMER HAVING CUSTOMER ID ["+customerId+"]");
		Flux<Order> ordersOfCustomerId = orderService.getAllOrdersByCustomerId(customerId);

		return new ResponseEntity(ordersOfCustomerId, HttpStatus.OK);
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * API TO FETCH ORDER BY PRODUCT ID
	 *
	 * @param productId
	 * @return
	 * @throws OrderServiceException
	 */

	@GetMapping("/getOrderDetails/{productId}")
	@CircuitBreaker(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@Retry(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@RateLimiter(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	public ResponseEntity<Order> getOrderDetailsByProductId(@PathVariable("productId") int productId) throws OrderServiceException{

		LOGGER.info("FETCHING ORDER DETAILS BY THE PRODUCT ID ["+productId+"]");
		Mono<Order> orderDetails = orderService.getOrderDetailsByProductId(productId);

		return new ResponseEntity(orderDetails, HttpStatus.OK);
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * API TO FETCH DELETE ORDER BY ORDER ID OR ORDER SERIAL KEY
	 *
	 * @param orderIdOrSerialKey
	 * @return
	 */

	@DeleteMapping("/delete/{orderIdOrSerialKey}")
	@CircuitBreaker(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@Retry(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	@RateLimiter(name = ORDER_SERVICE,fallbackMethod = "orderFallback")
	public ResponseEntity<String> deleteByOrderSerialKey(@PathVariable("orderIdOrSerialKey") String orderIdOrSerialKey) {

		if(orderIdOrSerialKey.matches("\\d+")){
			LOGGER.info("FETCHING.. INFO TO DELETE PROFILE WITH CUSTOMER ID ["+orderIdOrSerialKey+"]");
			orderService.deleteByOrderId(Integer.valueOf(orderIdOrSerialKey));
			return new ResponseEntity("CUSTOMER PROFILE DELETED SUCCESSFULLY",HttpStatus.ACCEPTED);
		}else{
			LOGGER.info("FETCHING.. INFO TO DELETE PROFILE WITH CUSTOMER EMAIL ID ["+orderIdOrSerialKey+"]");
			orderService.deleteByOrderSerialKey(orderIdOrSerialKey);
			return new ResponseEntity("CUSTOMER PROFILE DELETED SUCCESSFULLY",HttpStatus.ACCEPTED);
		}
	}

	@GetMapping("/runningStatus")
	public ResponseEntity<String> checkServiceStatus(){

		return new ResponseEntity("Service is Up and Running",HttpStatus.OK);
	}

	public ResponseEntity<?> orderFallback(Exception e){
		return new ResponseEntity("ERROR, WE ARE UNABLE TO COMMUNICATE WITH ORDER SERVICE",HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
