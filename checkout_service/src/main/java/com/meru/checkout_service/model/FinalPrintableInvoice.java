package com.meru.checkout_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection ="FINAL_PRINTABLE_INVOICE")
public class FinalPrintableInvoice {

    @Field(name = "ORDER_SERIAL_KEY")
    private String orderSerialKey;

    @Field(name = "ORDER_DATE")
    private String orderDate;

    @Field(name = "TOTAL_ORDER_PRICE")
    private int totalOrderPrice;

    @Field(name = "DELIVERY_ADDRESS")
    private Address deliveryAddress;

    @Field(name = "PAYMENT_METHOD")
    private String finalPaymentMode;


}
