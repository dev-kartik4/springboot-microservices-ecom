package com.meru.order_service_reactive.pojo;

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

	private int promotionId;

	private String offerDetails;

	private double discountedPercentage;

	private String expiryDate;
}
