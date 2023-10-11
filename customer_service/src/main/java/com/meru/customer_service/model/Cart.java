package com.meru.customer_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    private int customerIdForCart;

    public Set<CartProducts> cartProducts;

    public int totalPrice;
}
