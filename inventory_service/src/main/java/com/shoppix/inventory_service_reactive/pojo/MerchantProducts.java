package com.shoppix.inventory_service_reactive.pojo;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

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
