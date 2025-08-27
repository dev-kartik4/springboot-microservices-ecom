package com.shoppix.cart_service_reactive.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProduct {

    @Field(name = "PRODUCT_ID")
    private String productId;

    @Field(name = "PRODUCT_NAME")
    private String productName;

    @Field(name = "PRODUCT_IMAGE")
    private String productImageToShow;

    @Field(name = "PRODUCT_OPTIONS")
    private Map<String,String> productOptions;

    @Field(name = "STOCK_STATUS")
    private String stockStatus;

    @Field(name = "PRODUCT_PRICE")
    private double price;

    @Field(name = "QUANTITY")
    private int quantity;
}
