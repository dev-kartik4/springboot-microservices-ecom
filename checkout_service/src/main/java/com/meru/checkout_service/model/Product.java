package com.meru.checkout_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.TreeSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

	@Transient
	public static final String SEQUENCE_NAME = "product_sequence";

	private int productId;

	private String productSerialNumber;

	private String productName;

	private double price;

	private String modelNumber;

	private String dimensions;

	private String category;

	private String stockStatus;

	private TreeSet<String> promosAvailable;
}
