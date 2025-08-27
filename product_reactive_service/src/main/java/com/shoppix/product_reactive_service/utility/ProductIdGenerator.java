package com.shoppix.product_reactive_service.utility;

import com.shoppix.product_reactive_service.entity.Product;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;
import java.util.UUID;

@Component
public class ProductIdGenerator {

    public int generateRandomNumber(){
        Random randomObj = new Random();
        int randomNumber = randomObj.nextInt(1000);
        return randomNumber;

    }
    public String generateUniqueID() {

        return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}
