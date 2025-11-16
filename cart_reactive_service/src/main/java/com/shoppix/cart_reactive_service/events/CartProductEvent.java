package com.shoppix.cart_reactive_service.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shoppix.cart_reactive_service.entity.CartProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartProductEvent {

    @Id
    private List<String> productOrVariantProductIdList;

    private int customerIdForCart;

    private String cartProductMessageType;

    private CartProduct cartProduct;
}
