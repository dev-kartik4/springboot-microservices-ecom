package com.meru.order_service_reactive.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class OrderUtil {

    public String generateOrderId(){
        LocalDate d = LocalDate.now();
        LocalTime t = LocalTime.now();
        String orderId = d.getYear()+""+d.getMonthValue()+""+d.getDayOfMonth()+"-"+t.getHour()+""+t.getMinute()+""+t.getSecond();
        return orderId;
    }
}
