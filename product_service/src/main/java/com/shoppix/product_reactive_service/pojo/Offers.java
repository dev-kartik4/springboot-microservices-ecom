package com.shoppix.product_reactive_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offers {
	
	private int offerId;

	private String offerType;
	
	private String offerDescription;

	private double discountedPercentage;

	private boolean canClubWithMultipleOffers;

	private boolean canShowUpAsSpecialOffers;

	private String offersForCustomerSegment;

	private String offerStatus;

	private List<String> offersOnBrands;

	private List<String> offersByCategory;

	private String shippingRestrictions;

	private Date offerValidFrom;

	private Date offerValidTo;
}
