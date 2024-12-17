package com.shoppix.customer_service_reactive.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProduct {

    private int productId;

    private String productName;

    private String stockStatus;

    private double price;

    private int quantity;
}
