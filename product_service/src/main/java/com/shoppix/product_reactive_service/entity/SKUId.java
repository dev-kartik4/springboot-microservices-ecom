package com.shoppix.product_reactive_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SKU_ID_DETAILS")
public class SKUId {

    @Id
    @Field(name = "SKU_ID")
    private String skuId;  // MongoDB automatically generates this

    @Field(name = "SKU_CODE")
    private String skuCode; // SKU code for the variation (e.g., size, color)

    @DBRef// Reference to Product
    @Field(name = "SKU_LINK_TO_PRODUCT")
    private Product product;  // Linking to Product

    @Field(name = "PRODUCT_VARIATION")
    private String productVariation; // E.g., Size, Color

    @Field(name = "QUANTITY_IN_STOCK")
    private int quantityInStock; // How many units of this SKU are in stock
}
