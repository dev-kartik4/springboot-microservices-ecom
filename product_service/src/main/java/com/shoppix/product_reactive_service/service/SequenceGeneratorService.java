package com.shoppix.product_reactive_service.service;

import com.shoppix.product_reactive_service.pojo.Sequence;
import com.shoppix.product_reactive_service.repo.SequenceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SequenceGeneratorService {

    @Autowired
    private SequenceRepo sequenceRepository;

    // Method to get the next sequence number for a given sequence name
    public Mono<Long> generateNextSequence(String sequenceName) {
        // Find the sequence document by name, and increment the seq field
        return sequenceRepository.findById(sequenceName)
                .flatMap(sequence -> {
                    // If sequence exists, increment seq
                    sequence.setSequenceValue(sequence.getSequenceValue() + 1);
                    return sequenceRepository.save(sequence);  // Save the updated sequence
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // If sequence document doesn't exist, create it with an initial value of 1
                    Sequence newSequence = new Sequence();
                    newSequence.setId(sequenceName);
                    newSequence.setSequenceValue(1);
                    return sequenceRepository.save(newSequence);
                }))
                .map(Sequence::getSequenceValue);  // Return the new sequence number
    }

    // Generate SKU based on product information
    public String generateSKUCode(String category, String brand, String color, String size) {
        return category + "-" + brand + "-" + color + "-" + size;  // SKU generation logic
    }

    public Mono<Long> generateSkuId(String sequenceName) {
        // Find the sequence document by name, and increment the seq field
        return sequenceRepository.findById(sequenceName)
                .flatMap(sequence -> {
                    // If sequence exists, increment seq
                    sequence.setSequenceValue(sequence.getSequenceValue() + 1);
                    return sequenceRepository.save(sequence);  // Save the updated sequence
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // If sequence document doesn't exist, create it with an initial value of 1
                    Sequence newSequence = new Sequence();
                    newSequence.setId(sequenceName);
                    newSequence.setSequenceValue(1);
                    return sequenceRepository.save(newSequence);
                }))
                .map(Sequence::getSequenceValue);  // Return the new sequence number
    }
}
