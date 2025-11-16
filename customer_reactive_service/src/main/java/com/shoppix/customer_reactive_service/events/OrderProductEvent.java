package com.shoppix.customer_reactive_service.events;

import com.shoppix.customer_reactive_service.model.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderProductEvent {

    private String orderId;

    private OrderRequest orderRequest;
}
