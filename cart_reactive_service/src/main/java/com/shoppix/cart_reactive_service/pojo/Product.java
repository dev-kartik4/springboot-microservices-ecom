package com.shoppix.cart_reactive_service.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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
/*
	@Field(name = "PRODUCT_SKUs")
	private List<SKU> skuList = new ArrayList<>();*/

	@Field(name = "PRODUCT_SERIAL_NUMBER")
	@JsonIgnore
	private String productSerialNumber;

	private String productName;

	private String productBrand;

	private double productPrice;

	private double discountedPrice;

	private List<ProductDescription> productDescription;

	private String productAvailabilityStatus;

	private double averageRating;

	@Field(name = "PRODUCT_IMAGES")
	private List<String> productImages;

/*	@Field(name = "OFFERS_AVAILABLE")
	private List<Offers> offersAvailable;

	@Field(name = "RATINGS_AND_REVIEWS")
	private List<RatingsAndReviews> ratingsAndReviews;

	@Field(name = "MERCHANT_DETAILS")
	private MerchantDetails merchantDetails;*/

}
