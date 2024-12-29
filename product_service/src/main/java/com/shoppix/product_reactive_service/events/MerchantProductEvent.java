package com.shoppix.product_reactive_service.events;

import com.shoppix.product_reactive_service.pojo.MerchantProducts;
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
