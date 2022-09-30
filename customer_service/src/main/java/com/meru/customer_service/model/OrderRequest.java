package com.meru.customer_service.model;

import java.util.List;

public class OrderRequest {

	private int orderId;
	
	private int addressId;
	
	private int customerId;
	
	private String status;
	
	private List<Integer> productIdList;

	public OrderRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OrderRequest(int orderId, int addressId, int customerId, String status, List<Integer> productIdList) {
		super();
		this.orderId = orderId;
		this.addressId = addressId;
		this.customerId = customerId;
		this.status = status;
		this.productIdList = productIdList;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getAddressId() {
		return addressId;
	}

	public void setAddressId(int addressId) {
		this.addressId = addressId;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
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
	
}
