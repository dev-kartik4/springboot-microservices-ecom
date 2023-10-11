package com.meru.order_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinalPrintableInvoice {

    private String orderSerialKey;

    private String orderDate;

    private double totalOrderPrice;

    private String productSerialNumber;

    private Address deliveryAddress;

    private String finalPaymentMode;


}
