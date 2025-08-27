package com.shoppix.checkout_reactive_service.entity;

import com.shoppix.checkout_reactive_service.model.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class PaymentCheckout {

    private OrderRequest orderRequest;

    private String paymentOptionSelected;
}
