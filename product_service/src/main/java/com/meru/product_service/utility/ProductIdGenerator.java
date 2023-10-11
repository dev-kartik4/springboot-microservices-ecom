package com.meru.product_service.utility;

import com.meru.product_service.entity.Product;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

@Component
public class ProductIdGenerator {

    public int generateRandomNumber(){
        Random randomObj = new Random();
        int randomNumber = randomObj.nextInt(1000);
        return randomNumber;

    }
    public String generateProductId(Product prod){
        LocalDate d = LocalDate.now();
        LocalTime t = LocalTime.now();
        String productId =d.getYear()+""+d.getMonthValue()+""+d.getDayOfMonth()+"-"+t.getHour()+""+t.getMinute()+""+t.getSecond();
        return productId;
    }
}
