package com.shoppix.merchant_service_reactive.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantProduct {

    @Field(name = "MERCHANT_ID")
    private long merchantId;

    @Field(name = "MERCHANT_SELLING_NAME")
    private String merchantSellingName;

    @Field(name = "PARENT_PRODUCT_ID")
    private String parentProductId;

    @Field(name = "PARENT_PRODUCT_NAME")
    private String productName;

    @Field(name = "INVENTORY_ID")
    private long inventoryId;

    @Field(name = "INVENTORY_CODE_ASSIGNED_TO_MERCHANT")
    private String inventoryCode;
}
