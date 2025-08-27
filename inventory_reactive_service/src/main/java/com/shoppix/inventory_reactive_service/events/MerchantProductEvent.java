package com.shoppix.inventory_reactive_service.events;

import com.shoppix.inventory_reactive_service.pojo.MerchantProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantProductEvent {

    private long merchantId;

    private String merchantMessageType;

    private MerchantProduct merchantProduct;
}
