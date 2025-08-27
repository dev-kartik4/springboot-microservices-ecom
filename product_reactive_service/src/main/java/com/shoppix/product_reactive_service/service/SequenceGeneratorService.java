package com.shoppix.product_reactive_service.service;

import com.shoppix.product_reactive_service.pojo.Sequence;
import com.shoppix.product_reactive_service.repo.SequenceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SequenceGeneratorService {

    @Autowired
    private SequenceRepo sequenceRepository;

    //Generate UUID based on product ID
    public String generateProductUniqueID(String category) {

        String uuid = UUID.randomUUID().toString();
        return category.substring(0,3)+uuid.substring(0, 10).toUpperCase();
    }

    // Generate SKU based on product information
    public String generateSKUCode(String category, String subCategory, String brand, String color, String size) {
        return category.substring(0, 3) + "-" + subCategory.substring(0, 3) + "-" + brand.substring(0,3) +"-" + color.substring(0,3) + "-" + size.substring(0,3);  // SKU generation logic
    }
}
