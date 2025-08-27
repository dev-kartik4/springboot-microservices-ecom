package com.shoppix.merchant_reactive_service.events;

import com.shoppix.merchant_reactive_service.entity.MerchantProduct;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantProductEvent {

    private long merchantId;

    private String merchantMessageType;

    private MerchantProduct merchantProduct;
}

