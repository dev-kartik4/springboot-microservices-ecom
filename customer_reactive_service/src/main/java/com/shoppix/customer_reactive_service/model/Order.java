package com.shoppix.customer_reactive_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private String orderId;

    private String orderSerialKey;

    private int customerId;

    private String customerEmailId;

    private Set<Integer> productIdList;

    private int orderedQuantity;

    private String orderDate;

    private String status;
}
