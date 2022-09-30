package com.meru.cart_service.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="CART_DETAILS")
public class Cart {

	@Id
	@Column(name="CART_ID")
	private int cartId;
	
	@Column(name="CUSTOMER_ID")
	private int customerId;

	@OneToMany(cascade = CascadeType.ALL)
	public List<CartProducts> cartProducts;

	public Cart() {
	}

	public Cart(int cartId, int customerId, List<CartProducts> cartProducts) {
		this.cartId = cartId;
		this.customerId = customerId;
		this.cartProducts = cartProducts;
	}

	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public List<CartProducts> getCartProducts() {
		return cartProducts;
	}

	public void setCartProducts(List<CartProducts> cartProducts) {
		this.cartProducts = cartProducts;
	}

	@Override
	public String toString() {
		return "Cart{" +
				"cartId=" + cartId +
				", customerId=" + customerId +
				", cartProducts=" + cartProducts +
				'}';
	}



}
