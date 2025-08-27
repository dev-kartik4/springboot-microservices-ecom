package com.shoppix.customer_service_reactive.events;

import com.shoppix.customer_service_reactive.model.Cart;
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
