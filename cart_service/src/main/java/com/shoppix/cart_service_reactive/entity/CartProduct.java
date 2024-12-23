package com.shoppix.cart_service_reactive.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProduct {

    @Id
    private int productId;

    private String productName;

    private int price;

    private int quantity;
}
