package com.shoppix.merchant_service_reactive.events;

import com.shoppix.merchant_service_reactive.entity.MerchantDetails;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantEvent {

    private long merchantId;

    private String merchantMessageType;

    private MerchantDetails merchantDetails;
}
