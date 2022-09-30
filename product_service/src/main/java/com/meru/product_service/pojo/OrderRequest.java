package com.meru.product_service.pojo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class OrderRequest {
	
	private long orderId;

	private java.math.BigDecimal addressId;

	private java.math.BigDecimal customerId;

	private Date orderDate;

	private String status;

	private List<Integer> productIdList;

	public OrderRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OrderRequest(long orderId, BigDecimal addressId, BigDecimal customerId, Date orderDate, String status,
			List<Integer> productIdList) {
		super();
		this.orderId = orderId;
		this.addressId = addressId;
		this.customerId = customerId;
		this.orderDate = orderDate;
		this.status = status;
		this.productIdList = productIdList;
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}

	public java.math.BigDecimal getAddressId() {
		return addressId;
	}

	public void setAddressId(java.math.BigDecimal addressId) {
		this.addressId = addressId;
	}

	public java.math.BigDecimal getCustomerId() {
		return customerId;
	}

	public void setCustomerId(java.math.BigDecimal customerId) {
		this.customerId = customerId;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Integer> getProductIdList() {
		return productIdList;
	}

	public void setProductIdList(List<Integer> productIdList) {
		this.productIdList = productIdList;
	}

	@Override
	public String toString() {
		return "OrderRequest [orderId=" + orderId + ", addressId=" + addressId + ", customerId=" + customerId
				+ ", orderDate=" + orderDate + ", status=" + status + ", productIdList=" + productIdList + "]";
	}
}
