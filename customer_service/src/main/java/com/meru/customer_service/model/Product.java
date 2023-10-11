package com.meru.customer_service.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

	private long productId;
	
	private String productName;
	
	private double price;
	
	private String modelNumber;
	
	private String dimensions;
	
	private String category;
}
