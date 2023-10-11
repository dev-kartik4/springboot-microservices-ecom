package com.meru.order_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.meru.order_service.bean.OrderRequest;
import com.meru.order_service.exception.OrderServiceException;
import com.meru.order_service.pojo.*;
import com.meru.order_service.util.OrderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.meru.order_service.bean.Order;
import com.meru.order_service.repo.OrderRepo;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {
	
	@Autowired
	public OrderRepo orderRepo;

	@Autowired
	public OrderUtil orderUtil;

	@Autowired
	public RestTemplate restTemplate;

	@Autowired
	public SequenceGeneratorService sequenceGeneratorService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

	public ResponseEntity<Order> saveOrderRequest(OrderRequest orderRequest) {

		try{
			FinalPrintableInvoice finalPrintableInvoice = new FinalPrintableInvoice();
			LocalDate productOrderDate = LocalDate.now();
			List<Order> myOrderDetails = orderRepo.findAllByCustomerId(orderRequest.getCustomerId());
			//Optional<Order> order1 = Optional.ofNullable(orderRepo.findOrderDetailsByProductId(orderRequest.getProductId()));
			String pattern="ORDER#"+"-";
			String orderKey = orderUtil.generateOrderId();
			int orderId = (int) sequenceGeneratorService.generateSequence(Order.SEQUENCE_NAME);

			boolean orderPresent = myOrderDetails.stream().anyMatch(order -> order.getProductIdList().contains(orderRequest.getProductId()));
			Inventory inventory = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+orderRequest.getProductId(),Inventory.class);
			if(inventory.getQuantity() >= 2000 && inventory.getQuantity() <= 6000 ){
				LOGGER.info("LIMITED STOCK AVAILABLE");
				Optional<Order> newOrder = Optional.of(new Order());
				newOrder.get().setOrderId(orderId);
				newOrder.get().setOrderSerialKey(pattern+orderKey+"-#"+orderId);
				newOrder.get().setCustomerId(orderRequest.getCustomerId());
				newOrder.get().setCustomerEmailId(orderRequest.getCustomerEmailId());
				newOrder.get().setProductIdList(Collections.singleton(orderRequest.getProductId()));
				newOrder.get().setOrderDate(productOrderDate.toString());
				newOrder.get().setOrderedQuantity(orderRequest.getOrderRequestQuantity());
				newOrder.get().setStatus("ORDER SUCCESSFUL");
				orderRepo.save(newOrder.get());
				LOGGER.info("UPDATING ORDER LIST OF CUSTOMER");
				restTemplate.put("http://customer-service/customer/updateOrderList",newOrder.get(),Order.class);
				LOGGER.info("ORDER WITH PRODUCT ID ["+newOrder.get().getProductIdList()+"] PLACED FOR CUSTOMER ID ["+newOrder.get().getCustomerId()+"]");
				LOGGER.info("UPDATING PRODUCT STOCK");
//				Inventory inventory = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+orderRequest.getProductId(),Inventory.class);
				inventory.setInventoryId(inventory.getInventoryId());
				inventory.setStatus(inventory.getStatus());
				inventory.setQuantity(inventory.getQuantity()-orderRequest.getOrderRequestQuantity());
				inventory.setProductId(inventory.getProductId());
				restTemplate.put("http://inventory-service/updateInventory/"+inventory.getInventoryId(),inventory,Inventory.class);
				LOGGER.info("GENERATING FINAL PRINTABLE INVOICE");
				finalPrintableInvoice.setOrderSerialKey(newOrder.get().getOrderSerialKey());
				finalPrintableInvoice.setOrderDate(productOrderDate.toString());
				finalPrintableInvoice.setTotalOrderPrice(orderRequest.getTotalOrderPrice());
				finalPrintableInvoice.setFinalPaymentMode(orderRequest.getPaymentModeSelected());
				Address defaultDeliveryAddress = restTemplate.getForObject("http://customer-service/getDefaultAddress/"+orderRequest.getCustomerEmailId(), Address.class);
				finalPrintableInvoice.setDeliveryAddress(defaultDeliveryAddress);
				restTemplate.postForObject("http://checkout-service/checkout/generateFinalInvoice",finalPrintableInvoice,FinalPrintableInvoice.class);
				return new ResponseEntity(newOrder, HttpStatus.CREATED);
			}else{
				LOGGER.info("LIMITED OR NO STOCK AVAILABLE CURRENTLY");
				throw new OrderServiceException("ERROR PLACING ORDER ! CURRENTLY LIMITED OR NO STOCK AVAILABLE");
			}
		}catch(OrderServiceException orderEx){
			LOGGER.error("ERROR PLACING ORDER ! CURRENTLY UNAVAILABLE");
			orderEx.printStackTrace();
			throw new OrderServiceException("ERROR PLACING ORDER ! CURRENTLY UNAVAILABLE");
		}
	}
	
	public Optional<Order> getOrderById(int orderId) {
		try {
			Optional<Order> orderData = orderRepo.findById(orderId);
			if(orderData.isPresent()){
				LOGGER.info("FETCHING ORDER DETAILS WITH ORDER ID ["+orderId+"] SUCCESSFULL");
				return orderData;
			}else{
				throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH ORDER ID ["+orderId+"]");
			}
		}catch (OrderServiceException orderEx){
			LOGGER.error("ERROR FETCHING ORDER DETAILS WITH ORDER ID ["+orderId+"]");
			orderEx.printStackTrace();
			throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH ORDER ID ["+orderId+"]");
		}
	}

	public Optional<Order> getOrderByOrderSerialKey(String orderSerialKey){

		try {
			Optional<Order> orderData = Optional.ofNullable(orderRepo.findByOrderSerialKey(orderSerialKey));
			if(orderData.isPresent()){
				LOGGER.info("FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+orderSerialKey+"] SUCCESSFULL");
				return orderData;
			}else{
				throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+orderSerialKey+"]");
			}
		}catch (OrderServiceException orderEx){
			LOGGER.error("ERROR FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+orderSerialKey+"]");
			orderEx.printStackTrace();
			throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+orderSerialKey+"]");
		}
	}

	public boolean deleteByOrderId(int orderId) {
		boolean orderDeletedById = false;
		if(!orderDeletedById){
			orderRepo.deleteById(orderId);
			orderDeletedById = true;
		}
		return orderDeletedById;
	}

	public List<Order> getAllOrdersByCustomerId(int customerId) {
		try{
			List<Order> orders = new ArrayList<>();
			orders = orderRepo.findAllByCustomerId(customerId);
			if(!orders.isEmpty()){
				return orders;
			}else{
				throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+customerId+"]");
			}
		}catch (OrderServiceException orderEx){
			LOGGER.error("ERROR FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+customerId+"]");
			orderEx.printStackTrace();
			throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH ORDER SERIAL KEY ["+customerId+"]");
		}

	}

	public Order getOrderDetailsByProductId(int productId){

		try{
			List<Order> customerOrderDetails = new ArrayList<>();
			Order orderData = orderRepo.findOrderDetailsByProductId(productId);
			if(orderData != null){
				return orderData;
			}else{
				throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH PRODUCT ID ["+productId+"]");
			}
		}catch (OrderServiceException orderEx){
			LOGGER.error("ERROR FETCHING ORDER DETAILS WITH PRODUCT ID ["+productId+"]");
			orderEx.printStackTrace();
			throw new OrderServiceException("ERROR FETCHING ORDER DETAILS WITH PRODUCT ID ["+productId+"]");
		}
	}

	public boolean deleteByOrderSerialKey(String orderSerialKey){

		boolean orderDeletedBySerialKey = false;
		if(!orderDeletedBySerialKey){
			orderRepo.deleteByOrderSerialKey(orderSerialKey);
			orderDeletedBySerialKey = true;
		}
		return orderDeletedBySerialKey;
	}
}
