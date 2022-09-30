package com.meru.product_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

	private int inventoryId;
	
	private int quantity;
	
	private String status;

	private int productId;
}
