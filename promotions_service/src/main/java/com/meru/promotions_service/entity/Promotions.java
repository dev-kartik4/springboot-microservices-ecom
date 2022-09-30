package com.meru.promotions_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection = "PROMOTIONS_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotions {
	
	@Id
	@Field(name = "PROMOTION_ID")
	private int promotionId;
	
	@Field(name = "OFFER_DETAILS")
	private String offerDetails;

	@Field(name = "DISCOUNTED_PERCENTAGE")
	private double discountedPercentage;
	
	@Field(name = "EXPIRY_DATE")
	private String expiryDate;
}
