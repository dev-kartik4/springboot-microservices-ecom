package com.shoppix.customer_reactive_service.events;

import com.shoppix.customer_reactive_service.model.Cart;
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

    private CartProductEvent cartProductEvent;
}
