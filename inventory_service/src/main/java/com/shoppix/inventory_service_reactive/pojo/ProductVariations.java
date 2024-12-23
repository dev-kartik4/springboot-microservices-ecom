package com.shoppix.inventory_service_reactive.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVariations {

    private String variantProductId;

    private String skuCode;

    private int quantityOfStock;

    private String brand;

    private double mrpProductPrice;

    private double listingPrice;

    private double discountedPrice;

    private String productAvailabilityStatus;

    private double averageRating;

    private List<String> productImages;

    private List<Offers> offersAvailable;

    private List<RatingsAndReviews> ratingsAndReviews;

}
