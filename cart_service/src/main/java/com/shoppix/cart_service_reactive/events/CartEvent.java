package com.shoppix.cart_service_reactive.events;

import com.shoppix.cart_service_reactive.entity.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartEvent {

    private int customerIdForCart;

    private String cartMessageType;

    private Cart cartMessage;
}