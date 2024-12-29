package com.shoppix.product_reactive_service.pojo;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantProducts {

    private long merchantId;

    private String merchantSellingName;

    private String parentProductId;

    private String productName;

    private long inventoryId;

    private String inventoryCode;


}
