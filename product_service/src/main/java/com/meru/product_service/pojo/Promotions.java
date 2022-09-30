package com.meru.product_service.pojo;

import com.meru.product_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotions {
	
	private int promotionId;
	
	private String offerDetails;

	private double discountedPercentage;

	private String expiryDate;
}
