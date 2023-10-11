package com.meru.checkout_service.entity;

import com.meru.checkout_service.model.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class PaymentCheckout {

    private OrderRequest orderRequest;

    private String paymentOptionSelected;
}
