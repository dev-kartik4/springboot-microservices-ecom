package com.meru.order_service.bean;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="ORDER_DETAILS")
public class Order {
	
	@Id
	@Column(name="ORDER_ID")
	private int orderId;
	
	@Column(name="CUSTOMER_ID")
	private int customerId;
	
	@Column(name="ADDRESS_ID")
	private int addressId;
	
	@Column(name="PRODUCT_ID")
	private int productId;
	
	@Column(name="ORDER_DATE")
	private String orderDate;
	
	@Column(name="ORDER_STATUS")
	private String status;

	public Order() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Order(int orderId, int customerId, int addressId, int productId, String orderDate,String status) {
		super();
		this.orderId = orderId;
		this.customerId = customerId;
		this.addressId = addressId;
		this.productId = productId;
		this.orderDate = orderDate;
		this.status = status;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public int getAddressId() {
		return addressId;
	}

	public void setAddressId(int addressId) {
		this.addressId = addressId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", customerId=" + customerId + ", addressId=" + addressId + ", productId="
				+ productId + ", orderDate=" + orderDate + ", status=" + status + "]";
	}
	
	
	
}
