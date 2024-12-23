package com.shoppix.merchant_service_reactive.events;

import com.shoppix.merchant_service_reactive.entity.MerchantDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantEvent {

    private long merchantId;

    private String merchantName;

    private String merchantMessageType;

    private MerchantDetails merchantDetails;
}
