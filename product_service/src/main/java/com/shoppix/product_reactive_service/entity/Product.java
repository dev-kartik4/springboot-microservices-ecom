package com.shoppix.product_reactive_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "PRODUCT_DETAILS")
public class Product {

	@Transient
	public static final String SEQUENCE_NAME = "product_sequence";

	@Field(name = "PARENT_PRODUCT_ID")
	@JsonIgnore
	private String parentProductId;

	@Field(name = "PRODUCT_NAME")
	private String productName;

	@Field(name = "PRODUCT_VARIATIONS")
	private List<ProductVariations> productVariations;
	
	@Field(name = "CATEGORY")
	private String category;

	@Field(name = "SUBCATEGORY")
	private String subCategory;

	@Field(name = "FULFILLED_BY")
	private String productFulfillmentChannel;

	@Field(name = "PRODUCT_MANUFACTURER")
	private String productManufacturer;

	@Field(name = "PRODUCT_SELLER")
	private String productSeller;

	@Field(name = "PRODUCT_EVENT_STATUS")
	private String eventStatus;

	@Field(name = "CREATED_DATE_TIME")
	private String createdDateTime;

	@Field(name = "LAST_UPDATED_DATE_TIME")
	private String lastUpdatedDateTime;
}
