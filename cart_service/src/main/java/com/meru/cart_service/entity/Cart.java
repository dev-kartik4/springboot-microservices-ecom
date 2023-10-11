package com.meru.cart_service.entity;

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
	public Set<CartProducts> cartProducts;

	@Field(name = "TOTAL_PRICE")
	public int totalPrice;
}
