package com.shoppix.order_reactive_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

	private int inventoryId;
	
	private int quantity;
	
	private String status;

	private int productId;
}
