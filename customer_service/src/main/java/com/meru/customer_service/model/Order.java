package com.meru.customer_service.model;

import lombok.*;

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
