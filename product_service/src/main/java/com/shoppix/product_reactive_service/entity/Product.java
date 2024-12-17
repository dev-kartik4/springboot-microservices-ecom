package com.shoppix.product_reactive_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shoppix.product_reactive_service.pojo.Offers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "PRODUCT_DETAILS")
public class Product {

	@Transient
	public static final String SEQUENCE_NAME = "product_sequence";

	@Id
	@Field(name = "PRODUCT_ID")
	@JsonIgnore
	private long productId;

	@Field(name = "PRODUCT_WITH_MANY_SKU")
	@DBRef
	private List<SKU> skuList = new ArrayList<>();

	@Field(name = "PRODUCT_SERIAL_NUMBER")
	@JsonIgnore
	private String productSerialNumber;

	@Field(name = "PRODUCT_NAME")
	private String productName;

	@Field(name = "PRODUCT_BRAND")
	private String productBrand;
	
	@Field(name = "PRODUCT_PRICE")
	private double productPrice;

	@Field(name = "DISCOUNTED_PRICE")
	private double discountedPrice;
	
	@Field(name = "MODEL_NUMBER")
	private String modelNumber;

	@Field(name = "PRODUCT_DESCRIPTIVE_DETAILS")
	private List<ProductDescription> productDescription;
	
	@Field(name = "CATEGORY")
	private String category;

	@Field(name = "PRODUCT_AVAILABILITY_STATUS")
	private String productAvailabilityStatus;

	@Field(name = "PRODUCT_FULFILLED_BY")
	private String productFulfillmentChannel;

	@Field(name = "PRODUCT_MANUFACTURER")
	private String productManufacturer;

	@Field(name = "PRODUCT_RATING")
	private double averageRating;

	@Field(name = "PRODUCT_IMAGES")
	private List<String> productImages;

	@Field(name = "OFFERS_AVAILABLE")
	private List<Offers> offersAvailable;

	@Field(name = "RATINGS_AND_REVIEWS")
	private List<RatingsAndReviews> ratingsAndReviews;

	@Field(name = "MERCHANT_DETAILS")
	private MerchantDetails merchantDetails;

	@Field(name = "PRODUCT_EVENT_STATUS")
	private String eventStatus;

	@Field(name = "CREATED_DATE_TIME")
	private String createdDateTime;

	@Field(name = "LAST_UPDATED_DATE_TIME")
	private String lastUpdatedDateTime;
}
