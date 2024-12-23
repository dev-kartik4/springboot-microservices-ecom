package com.shoppix.merchant_service_reactive.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Offers {

	@Field(name = "OFFER_ID")
	private int offerId;

	@Field(name = "OFFER_FOR_PRODUCT_ID")
	private String productId;

	@Field(name = "OFFER_FOR_PRODUCT_NAME")
	private String productName;

	@Field(name = "OFFER_TYPE")
	private String offerType;

	@Field(name = "OFFER_DESCRIPTION")
	private String offerDescription;

	@Field(name = "DISCOUNTED_PERCENTAGE")
	private double discountedPercentage;

	@Field(name = "CAN_CLUB_WITH_MULTIPLE_OFFERS?")
	private boolean canClubWithMultipleOffers;

	@Field(name = "CAN_SHOW_UP_AS_SPECIAL_OFFERS?")
	private boolean canShowUpAsSpecialOffers;

	@Field(name = "OFFERS_FOR_CUSTOMER_SEGMENT")
	private String offersForCustomerSegment;

	@Field(name = "OFFER_STATUS")
	private String offerStatus;

	@Field(name = "OFFERS_ON_BRANDS")
	private List<String> offersOnBrands;

	@Field(name = "OFFERS_BY_CATEGORY")
	private List<String> offersByCategory;

	@Field(name = "SHIPPING_RESTRICTIONS")
	private String shippingRestrictions;

	@Field(name = "OFFER_VALID_FROM")
	private String offerValidFrom;

	@Field(name = "OFFER_VALID_UPTO")
	private String offerValidTo;
}
