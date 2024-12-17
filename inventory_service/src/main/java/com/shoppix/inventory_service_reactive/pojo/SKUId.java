package com.shoppix.inventory_service_reactive.pojo;

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
public class SKUId {


    private String skuId;  // MongoDB automatically generates this

    private String skuCode; // SKU code for the variation (e.g., size, color)

    private String productVariation; // E.g., Size, Color

    private int quantityInStock; // How many units of this SKU are in stock
}
