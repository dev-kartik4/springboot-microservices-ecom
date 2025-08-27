package com.shoppix.cart_reactive_service.events;

import com.shoppix.cart_reactive_service.entity.CartProduct;
import com.shoppix.customer_service_reactive.model.CartProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProductEvent {

    private int customerIdForCart;

    private String productId;

    private String cartProductMessageType;

    private CartProduct cartProduct;
}
