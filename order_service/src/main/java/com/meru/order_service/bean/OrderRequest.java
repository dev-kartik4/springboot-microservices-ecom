package com.meru.order_service.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

	@Id
	private int customerId;

	private String customerEmailId;

	@JsonIgnore
	private String status;

	private int productId;

	@JsonIgnore
	private String productSerialNumber;

	private int orderRequestQuantity;

	@JsonIgnore
	private double totalOrderPrice;

	private String paymentModeSelected;
}
