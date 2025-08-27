package com.shoppix.inventory_reactive_service.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVariations {

    private String variantProductId;

    private SKU skuData;

    private String brand;

    private double mrpProductPrice;

    private double listingPrice;

    private double discountedPrice;

    private String productAvailabilityStatus;

}
