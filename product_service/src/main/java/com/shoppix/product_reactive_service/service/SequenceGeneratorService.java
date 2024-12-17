package com.shoppix.product_reactive_service.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SequenceGeneratorService {

    private static final AtomicInteger sequence = new AtomicInteger(100); // Start sequence at 100

    /**
     * Generate the next product ID for the given sequence name.
     *
     * @param sequenceName The sequence name (e.g., Product.SEQUENCE_NAME).
     * @return A Mono containing the next product ID.
     */
    public Mono<Integer> generateNextSequence(String sequenceName) {
        // For simplicity, we're just returning the incremented value for the given sequence name
        // In a real scenario, you could store this sequence value in a database to make it persistent
        return Mono.just(sequence.getAndIncrement());
    }

    public String generateSKU(String category,String productBrand, String color, String size) {
        // Example SKU format: MODEL-COLOR-SIZE
        return category + "-" + productBrand + "-" + color + "-" + size;
    }
}
