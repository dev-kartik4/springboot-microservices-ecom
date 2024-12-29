package com.shoppix.merchant_service_reactive.util;

import com.shoppix.merchant_service_reactive.repo.MerchantRepo;
import com.shoppix.merchant_service_reactive.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MerchantUtil {

    @Autowired
    private MerchantService merchantService;

    public long generateMerchantId()
    {
        int min = 1000;
        int max = 99999999;
        return (int)(Math.random() * (max - min + 1) + min);
    }
}
