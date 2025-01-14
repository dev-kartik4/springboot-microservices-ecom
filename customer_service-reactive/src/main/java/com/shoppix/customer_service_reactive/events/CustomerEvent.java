package com.shoppix.customer_service_reactive.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEvent {

    private int customerId;

    private String customerName;

    private String customerEmail;

    private long phone;

    private String customerMessageType;


}
