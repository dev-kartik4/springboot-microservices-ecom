package com.meru.order_service_reactive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.TreeSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

	private int productId;

	private String productSerialNumber;

	private String productName;

	private double price;

	private String modelNumber;

	private String dimensions;

	private String category;

	private String stockStatus;

	private List<Promotions> promosAvailable;
}
