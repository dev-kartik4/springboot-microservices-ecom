package com.shoppix.cart_service_reactive.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "CART_DETAILS")
public class Cart {

	@Id
	@Field(name="CUSTOMER_CART_ID")
	private int customerIdForCart;

	@Field(name = "PRODUCTS_IN_CART")
	public List<CartProduct> cartProducts;

	@Field(name = "TOTAL_CART_PRICE")
	public double totalPrice;

	@Field(name = "CART_EVENT_STATUS")
	public String eventStatus;

	@Field(name = "LAST_UPDATED_DATE_TIME")
	public String lastUpdatedDateTime;
}
