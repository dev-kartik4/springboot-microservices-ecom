package com.shoppix.product_reactive_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDetails {

    @Id
    @Field(name = "MERCHANT_ID")
    private long merchantId;

    @Field(name = "MERCHANT_NAME")
    private String merchantName;

    @Field(name = "MERCHANT_ADDRESS")
    private String merchantFullAddress;

    @Field(name = "MERCHANT_BANK_ACCOUNT_NUMBER")
    private String merchantBankAccountNumber;

    @Field(name = "MERCHANT_BANKING_NAME")
    private String merchantBankingName;

    @Field(name = "MERCHANT_BANK_IFSC_CODE")
    private String merchantBankIfscCode;
}
