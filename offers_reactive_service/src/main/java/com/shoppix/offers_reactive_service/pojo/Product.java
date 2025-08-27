package com.shoppix.offers_reactive_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private int productId;

    private String productSerialNumber;

    private String productName;

    private double price;

    private String modelNumber;

    private String dimensions;

    private String category;

    private String stockStatus;

}
