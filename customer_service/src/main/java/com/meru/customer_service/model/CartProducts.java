package com.meru.customer_service.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProducts{

    private int productId;

    private String productName;

    private double price;

    private int quantity;
}
