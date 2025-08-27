package com.shoppix.product_reactive_service.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shoppix.product_reactive_service.pojo.Offers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVariations {

    @Field(name = "VARIANT_PRODUCT_ID")
    private String variantProductId;

    @Field(name = "SKU_CODE_DATA")
    private SKU skuData;

    @Field(name = "PRODUCT_DESCRIPTIVE_DETAILS")
    private ProductDescription productDescription;

    @Field(name = "BRAND")
    private String brand;

    @Field(name = "MRP_OF_PRODUCT")
    private double mrpProductPrice;

    @Field(name = "LISTING_PRICE")
    private double listingPrice;

    @Field(name = "DISCOUNTED_PRICE")
    private double discountedPrice;

    @Field(name = "PRODUCT_AVAILABILITY_STATUS")
    private String productAvailabilityStatus;

    @Field(name = "PRODUCT_RATING")
    private double averageRating;

    @Field(name = "PRODUCT_IMAGES")
    private List<String> productImages;

    @Field(name = "OFFERS_AVAILABLE")
    private List<Offers> offersAvailable;

    @Field(name = "RATINGS_AND_REVIEWS")
    private List<RatingsAndReviews> ratingsAndReviews;

}
