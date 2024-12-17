package com.shoppix.customer_service_reactive.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

	private int productId;

	private String productSerialNumber;

	private String productName;

	private double price;

	private String modelNumber;

	private String dimensions;

	private ProductDescription productDescription;

	private String category;

	private String stockStatus;

	private String productSeller;
}
