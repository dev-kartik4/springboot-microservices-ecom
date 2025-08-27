package com.shoppix.merchant_reactive_service.events;

import com.shoppix.merchant_reactive_service.entity.MerchantDetails;
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
