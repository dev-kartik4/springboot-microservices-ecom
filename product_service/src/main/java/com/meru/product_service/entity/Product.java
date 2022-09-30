package com.meru.product_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.TreeSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "PRODUCT_DETAILS")
public class Product {

	@Id
	@Field(name = "PRODUCT_ID")
	private int productId;
	
	@Field(name = "PRODUCT_NAME")
	private String productName;
	
	@Field(name = "PRICE")
	private double price;
	
	@Field(name = "MODEL_NUMBER")
	private String modelNumber;
	
	@Field(name = "DIMENSIONS")
	private String dimensions;
	
	@Field(name = "CATEGORY")
	private String category;

	@Field(name = "STOCK_STATUS")
	private String stockStatus;

//	@OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
//	@JoinColumn(name = "product_id",referencedColumnName = "product_id")
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "promotion_id",referencedColumnName = "promotion_id")
	@Field(name = "PROMOS_AVAILABLE")
	private TreeSet<String> promosAvailable;
}
