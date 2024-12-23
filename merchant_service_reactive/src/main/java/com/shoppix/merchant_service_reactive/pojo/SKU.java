package com.shoppix.merchant_service_reactive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SKU{

    @Id
    @Field(name = "SKU_CODE")
    private String skuCode; // SKU code for the variation (e.g., size, color)

    @Field(name = "SKU_PRODUCT_ID")
    private String productId;  // Linking to Product

    @Field(name = "QUANTITY_IN_STOCK")
    private int quantityInStock; // How many units of this SKU are in stock
}
