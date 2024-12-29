package com.shoppix.merchant_service_reactive.events;

import com.shoppix.merchant_service_reactive.entity.MerchantDetails;
import com.shoppix.merchant_service_reactive.entity.MerchantProducts;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantProductEvent {

    private long merchantId;

    private String merchantMessageType;

    private MerchantProducts merchantProducts;
}

