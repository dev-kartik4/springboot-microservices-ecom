package com.meru.order_service_reactive.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meru.order_service_reactive.pojo.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection ="ORDER_DETAILS")
public class Order {

	@Transient
	public static final String SEQUENCE_NAME = "order_sequence";

	@Id
	@Field(name="ORDER_ID")
	private long orderId;

	@Field(name = "ORDER_SERIAL_KEY")
	private String orderSerialKey;
	
	@Field(name="CUSTOMER_ID")
	private int customerId;

	@Field(name="CUSTOMER_EMAIL_ID")
	private String customerEmailId;

	@Field(name = "PRODUCT_ID")
	private int productId;

	@Field(name="PRODUCT_ORDERED")
	private Product product;

	@Field(name = "ORDERED_QUANTITY")
	private int orderedQuantity;
	
	@Field(name="ORDER_DATE")
	@JsonIgnore
	private String orderDate;
	
	@Field(name="ORDER_STATUS")
	@JsonIgnore
	private String status;
}
