package com.shoppix.order_reactive_service.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

import com.shoppix.order_reactive_service.bean.OrderRequest;
import com.shoppix.order_reactive_service.exception.OrderServiceException;
import com.shoppix.order_reactive_service.pojo.*;
import com.shoppix.order_reactive_service.util.OrderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.shoppix.order_reactive_service.bean.Order;
import com.shoppix.order_reactive_service.repo.OrderRepo;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class OrderService {

	@Autowired
	public OrderRepo orderRepo;

	@Autowired
	public OrderUtil orderUtil;

	@Autowired
	public WebClient.Builder webClientBuilder;

	@Autowired
	public SequenceGeneratorService sequenceGeneratorService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

	public static final String CUSTOMER_SERVICE_URL = "http://customer-service/customer";

	public static final String INVENTORY_SERVICE_URL = "http://inventory-service/inventory";

	public static final String PRODUCT_SERVICE_URL = "http://product-service/products";

	public static final String CHECKOUT_SERVICE_URL = "http://checkout-service/checkout";

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO CREATE AND SAVE NEW ORDER DETAILS
	 *
	 * @param orderRequest
	 * @return
	 * @throws OrderServiceException
	 */
	public Mono<Order> saveOrderRequest(OrderRequest orderRequest) throws OrderServiceException {

		AtomicBoolean orderExists = new AtomicBoolean(false);
		Mono<FinalPrintableInvoice> finalPrintableInvoice = Mono.just(new FinalPrintableInvoice());
		Mono<Order> newOrder = Mono.just(new Order());
		LocalDate productOrderDate = LocalDate.now();
		Flux<Order> myOrderList = orderRepo.findAllByCustomerId(orderRequest.getCustomerId());

		Mono<String> pattern = Mono.just("ORDER#");
		Mono<String> orderKey = Mono.just(orderUtil.generateOrderId());
		long orderId = sequenceGeneratorService.generateSequence(Order.SEQUENCE_NAME);
		Mono<Boolean> orderPresent = (Mono<Boolean>) myOrderList.toStream().filter(order -> {
			if(order.getProduct().getProductId() == orderRequest.getProductId()){
				orderExists.set(true);

			};
			return orderExists.get();
		});

		Mono<Inventory> inventory = webClientBuilder.build()
				.get()
				.uri(INVENTORY_SERVICE_URL.concat("/getInventory/").concat(String.valueOf(orderRequest.getProductId())))
				.retrieve()
				.bodyToMono(Inventory.class)
				.publishOn(Schedulers.parallel());

		inventory.map(stockInventory -> {
			Mono<Inventory> updatedInventory = null;
			Mono<Product> fetchedProduct = webClientBuilder.build()
					.get()
					.uri(PRODUCT_SERVICE_URL.concat("/getProduct/").concat(String.valueOf(orderRequest.getProductId())))
					.retrieve()
					.bodyToMono(Product.class)
					.publishOn(Schedulers.parallel());

			if (stockInventory.getQuantity() >= 2000 && stockInventory.getQuantity() <= 6000) {
				LOGGER.info("LIMITED STOCK AVAILABLE");
				newOrder.map(freshOrder -> {
					freshOrder.setOrderId(orderId);
					freshOrder.setOrderSerialKey(pattern + "-" + orderKey + "-#" + orderId);
					freshOrder.setCustomerId(orderRequest.getCustomerId());
					freshOrder.setCustomerEmailId(orderRequest.getCustomerEmailId());
					freshOrder.setProduct((Product) fetchedProduct.subscribe());
					freshOrder.setOrderDate(productOrderDate.toString());
					freshOrder.setOrderedQuantity(orderRequest.getOrderRequestQuantity());
					freshOrder.setStatus("ORDER SUCCESSFUL");
					orderRepo.save(freshOrder).subscribe();
					return freshOrder;
				});

				LOGGER.info("UPDATING ORDER LIST OF CUSTOMER");
				webClientBuilder.build()
						.put()
						.uri(CUSTOMER_SERVICE_URL.concat("/updateOrderList"))
						.bodyValue(newOrder)
						.retrieve()
						.toEntity(Customer.class)
						.publishOn(Schedulers.parallel());
				LOGGER.info("ORDER WITH PRODUCT DETAILS [" + newOrder.map(order -> order.getProduct())  + "] PLACED FOR CUSTOMER ID [" + newOrder.map(order -> order.getCustomerId()) + "]");

				LOGGER.info("FETCHING INVENTORY OR STOCK DETAILS FOR THE PRODUCT ID [" + orderRequest.getProductId() + "]");
				Mono<Inventory> inventoryStock = webClientBuilder.build()
						.get()
						.uri(INVENTORY_SERVICE_URL.concat("/getInventory/").concat(String.valueOf(orderRequest.getProductId())))
						.retrieve()
						.bodyToMono(Inventory.class)
						.publishOn(Schedulers.parallel());

				updatedInventory = inventoryStock.map(inventory1 -> {
					Inventory updatedStock = new Inventory();
					updatedStock.setInventoryId(inventory1.getInventoryId());
					updatedStock.setStatus(inventory1.getStatus());
					updatedStock.setQuantity(inventory1.getQuantity() - orderRequest.getOrderRequestQuantity());
					updatedStock.setProductId(inventory1.getProductId());
					return updatedStock;
				});
				LOGGER.info("UPDATING INVENTORY OR STOCK DETAILS FOR THE PRODUCT ID [" + orderRequest.getProductId() + "]");
				webClientBuilder.build()
						.put()
						.uri(INVENTORY_SERVICE_URL.concat("/updateInventory/" + updatedInventory.map(Inventory::getInventoryId)))
						.bodyValue(updatedInventory)
						.retrieve()
						.toEntity(Inventory.class)
						.publishOn(Schedulers.parallel());

				LOGGER.info("GENERATING FINAL PRINTABLE INVOICE");

				finalPrintableInvoice.map(finalInvoice -> {
					finalInvoice.setOrderSerialKey(String.valueOf(newOrder.map(Order::getOrderSerialKey)));
					finalInvoice.setOrderDate(productOrderDate.toString());
					finalInvoice.setTotalOrderPrice(orderRequest.getTotalOrderPrice());
					finalInvoice.setFinalPaymentMode(orderRequest.getPaymentModeSelected());
					Mono<Address> defaultDeliveryAddress = webClientBuilder.build()
							.get()
							.uri(CUSTOMER_SERVICE_URL.concat("/getDefaultAddress/").concat(orderRequest.getCustomerEmailId()))
							.retrieve()
							.bodyToMono(Address.class)
							.publishOn(Schedulers.parallel());

					finalInvoice.setDeliveryAddress(defaultDeliveryAddress);
					webClientBuilder.build()
							.post()
							.uri(CHECKOUT_SERVICE_URL.concat("/generateFinalInvoice"))
							.bodyValue(finalInvoice)
							.retrieve()
							.toEntity(FinalPrintableInvoice.class)
							.thenReturn(new ResponseEntity(newOrder, HttpStatus.CREATED));
					return finalInvoice;
				});
			}
			return updatedInventory;
		}).switchIfEmpty(Mono.error(() -> {
			LOGGER.info("ERROR PLACING ORDER ! CURRENTLY LIMITED OR NO STOCK AVAILABLE");
			throw new OrderServiceException("ERROR PLACING ORDER ! CURRENTLY LIMITED OR NO STOCK AVAILABLE");
		})).doOnNext(System.out::println).delaySubscription(Duration.ofMillis(3000));

		return newOrder;
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO FETCH ORDER DETAILS BY ORDER ID
	 * @param orderId
	 * @return
	 * @throws OrderServiceException
	 */
	public Mono<Order> getOrderById(long orderId) throws OrderServiceException {

		Mono<Order> orderData = orderRepo.findById(orderId);

		LOGGER.info("FETCHING ORDER DETAILS WITH ORDER ID ["+orderId+"]");

		return orderData.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR FETCHING ORDER DETAILS WITH ORDER ID ["+orderId+"]");
			throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH ORDER ID ["+orderId+"]");
		}));
	}

	/**
	 * WILL BE CONTROLLED BY ADMIN ONLY
	 * <p>
	 * METHOD TO FETCH ORDER DETAILS BY ORDER SERIAL KEY
	 *
	 * @param orderSerialKey
	 * @return
	 * @throws OrderServiceException
	 */
	public Mono<Order> getOrderByOrderSerialKey(String orderSerialKey) throws OrderServiceException {

		Mono<Order> orderData = orderRepo.findByOrderSerialKey(orderSerialKey);

		LOGGER.info("FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+orderSerialKey+"]");

		return orderData.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+orderSerialKey+"]");
			throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+orderSerialKey+"]");
		}));
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO FETCH ALL ORDER DETAILS AS A LIST FROM BEGINNING OF ACCOUNT CREATION
	 *
	 * @param customerId
	 * @return
	 * @throws OrderServiceException
	 */

	public Flux<Order> getAllOrdersByCustomerId(int customerId) throws OrderServiceException {

		LOGGER.info("IN PROCESS OF FETCHING ORDER DETAILS WITH CUSTOMER ID [ "+customerId+" ");
		Flux<Order> orders = orderRepo.findAllByCustomerId(customerId);
		return orders.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+customerId+"]");
			throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+customerId+"]");
		}));
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO FETCH ORDER DETAILS BY PRODUCT ID
	 *
	 * @param productId
	 * @return
	 * @throws OrderServiceException
	 */
	public Mono<Order> getOrderDetailsByProductId(int productId) throws OrderServiceException {

		Mono<Order> customerOrderDetails = orderRepo.findOrderDetailsByProductId(productId);

		LOGGER.info("FETCHING ORDER DETAILS WITH PRODUCT ID ["+productId+"]");

		return customerOrderDetails.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR FETCHING ORDER DETAILS WITH PRODUCT ID ["+productId+"]");
			throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH PRODUCT ID ["+productId+"]");
		})).delaySubscription(Duration.ofMillis(3000));
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO DELETE ORDER BY ORDER ID
	 *
	 * @param orderId
	 * @return
	 * @throws OrderServiceException
	 */
	public AtomicBoolean deleteByOrderId(long orderId) throws OrderServiceException {

		LOGGER.info("IN PROCESS OF DELETING ORDER DETAILS WITH ORDER ID [ "+orderId+" ");
		AtomicBoolean orderDeletedById = new AtomicBoolean(false);
		Mono<Order> orderExists = getOrderById(orderId);
		orderExists.publishOn(Schedulers.parallel()).filter(order -> {
			if(orderExists != null){
				orderRepo.deleteById(orderId);
				orderDeletedById.set(true);
				LOGGER.info("ORDER WITH ID ["+orderId+"] DELETED SUCCESSFULLY");
			}
			return true;
		}).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR DELETING ORDER WITH ORDER ID [ "+orderId+"]");
			throw new OrderServiceException("ERROR DELETING ORDER WITH ORDER ID [ "+orderId+"]");
		})).doOnNext(System.out::println).delaySubscription(Duration.ofMillis(3000));

		return orderDeletedById;
	}

	/**
	 * WILL BE CONTROLLED BY ADMIN ONLY
	 * <p>
	 * METHOD TO DELETE ORDER BY ORDER SERIAL KEY
	 *
	 * @param orderSerialKey
	 * @return
	 * @throws OrderServiceException
	 */
	public AtomicBoolean deleteByOrderSerialKey(String orderSerialKey) throws OrderServiceException {

		LOGGER.info("IN PROCESS OF DELETING ORDER DETAILS WITH ORDER SERIAL KEY [ "+orderSerialKey+"]");
		AtomicBoolean orderDeletedBySerialKey = new AtomicBoolean(false);
		Mono<Order> orderExists = getOrderByOrderSerialKey(orderSerialKey);
		orderExists.publishOn(Schedulers.parallel()).filter(order -> {
			if(orderExists != null){
				orderRepo.deleteByOrderSerialKey(orderSerialKey);
				orderDeletedBySerialKey.set(true);
				LOGGER.info("ORDER WITH SERIAL KEY ["+orderSerialKey+"] DELETED SUCCESSFULLY");
			}
			return true;
		}).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR DELETING ORDER WITH ORDER SERIAL KEY [ "+orderSerialKey+"]");
			throw new OrderServiceException("ERROR DELETING ORDER WITH ORDER SERIAL KEY [ "+orderSerialKey+"]");
		})).doOnNext(System.out::println).delaySubscription(Duration.ofMillis(3000));

		return orderDeletedBySerialKey;
	}
}
