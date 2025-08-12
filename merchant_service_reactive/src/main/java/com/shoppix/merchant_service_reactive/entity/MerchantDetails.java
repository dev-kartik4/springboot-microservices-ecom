package com.shoppix.merchant_service_reactive.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "MERCHANT_DETAILS")
public class MerchantDetails {

    @Id
    @Field(name = "MERCHANT_ID")
    private long merchantId;

    @Field(name = "MERCHANT_PERSON_NAME")
    private String merchantPersonName;

    @Field(name = "MERCHANT_SELLING_NAME")
    private String merchantSellingName;

    @Field(name = "MERCHANT_EMAIL_ADDRESS")
    private String merchantEmailAddress;

    @Field(name = "MERCHANT_CONTACT_NUMBER")
    private String merchantContactNumber;

    @Field(name = "MERCHANT_ADDRESS")
    private String merchantFullAddress;

    @Field(name = "MERCHANT_BANK_ACCOUNT_NUMBER")
    private String merchantBankAccountNumber;

    @Field(name = "MERCHANT_BANKING_NAME")
    private String merchantBankingName;

    @Field(name = "MERCHANT_BANK_IFSC_CODE")
    private String merchantBankIfscCode;

    @Field(name = "PAN_NUMBER")
    private String panNumber;

    @Field(name = "GSTIN_TAXINFO")
    private String gstInTaxInformation;

    @Field(name = "PRODUCTS_BY_MERCHANT")
    private List<MerchantProduct> listOfProductsByMerchant;

    @Field(name = "MERCHANT_EVENT_STATUS")
    private String eventStatus;

    @Field(name = "CREATED_DATE_TIME")
    private String createdDateTime;

    @Field(name = "LAST_UPDATED_DATE_TIME")
    private String lastUpdatedDateTime;

    @Field(name = "MERCHANT_ACCOUNT_EXISTENCE")
    private boolean merchantExistence;
}
