package com.meru.checkout_service_reactive.entity;

import com.meru.checkout_service_reactive.model.OrderRequest;
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
