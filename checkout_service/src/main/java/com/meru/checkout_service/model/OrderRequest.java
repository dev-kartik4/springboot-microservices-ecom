package com.meru.checkout_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
	
	private int customerId;

	private String customerEmailId;

	@JsonIgnore
	private String status;
	
	private int productId;

	private int orderRequestQuantity;

	@JsonIgnore
	private double totalOrderPrice;

	private String paymentModeSelected;

}
