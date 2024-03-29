package com.meru.cart_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProducts {

    @Id
    private int productId;

    private String productName;

    private int price;

    private int quantity;
}
