package com.shoppix.inventory_service_reactive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SKU {


    private String skuId;  // MongoDB automatically generates this

    private String skuCode; // SKU code for the variation (e.g., size, color)

    private String productVariation; // E.g., Size, Color

    private int quantityInStock; // How many units of this SKU are in stock
}
