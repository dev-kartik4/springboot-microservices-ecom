package com.shoppix.inventory_service_reactive.events;

import com.shoppix.inventory_service_reactive.pojo.MerchantProducts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantProductEvent {

    private long merchantId;

    private String merchantMessageType;

    private MerchantProducts merchantProducts;
}
