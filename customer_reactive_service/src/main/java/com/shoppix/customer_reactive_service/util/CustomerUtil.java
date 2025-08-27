package com.shoppix.customer_reactive_service.util;

import com.shoppix.customer_reactive_service.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerUtil {

    @Autowired
    public CustomerService customerService;

    public int generateCustomerId()
    {
        int min = 30000000;
        int max = 99999999;
        int customerID = (int)(Math.random() * (max - min + 1) + min);
        return customerID;
    }
}
