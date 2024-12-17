package com.shoppix.customer_service_reactive.model;

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

    public int totalPrice;

    public String eventStatus;

    public String lastUpdatedDateTime;
}
