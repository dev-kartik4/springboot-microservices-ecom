package com.shoppix.cart_reactive_service.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartProduct {

    @Id
    @Field(name = "PRODUCT_OR_VARIANT_PRODUCT_ID_LIST")
    private List<String> productOrVariantProductIdList;

    @Field(name = "PRODUCT_NAME")
    private String productName;

    @Field(name = "PRODUCT_IMAGE")
    private List<String> productImagesToShow;

    @Field(name = "STOCK_STATUS")
    private String stockStatus;

    @Field(name = "PRODUCT_LISTED_PRICE")
    private double listedPrice;

    @Field(name = "PRODUCT_DISCOUNTED_PRICE")
    private double discountedPrice;

    @Field(name = "QUANTITY")
    private int quantity;

    @Field(name = "PRODUCT_RATING")
    private double averageRating;
}
