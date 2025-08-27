package com.shoppix.order_reactive_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinalPrintableInvoice {

    private String orderSerialKey;

    private String orderDate;

    private double totalOrderPrice;

    private Mono<Address> deliveryAddress;

    private String finalPaymentMode;


}
