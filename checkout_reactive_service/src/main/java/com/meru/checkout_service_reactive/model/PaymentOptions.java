package com.meru.checkout_service_reactive.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOptions {

    private boolean optedForCreditOrDebitCard;

    private boolean optedForNetBanking;

    private boolean optedForEmiFacility;

    private boolean optedForCashOnDelivery;
}
