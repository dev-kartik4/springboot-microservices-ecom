package com.shoppix.inventory_service_reactive.pojo;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantProduct {

    private long merchantId;

    private String merchantSellingName;

    private String parentProductId;

    private String productName;

    private long inventoryId;

    private String inventoryCode;


}
