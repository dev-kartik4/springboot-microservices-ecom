package com.shoppix.customer_reactive_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    public int customerIdForCart;

    public List<CartProduct> cartProducts;

    public double totalPrice;

    public String eventStatus;

    public String lastUpdatedDateTime;
}
