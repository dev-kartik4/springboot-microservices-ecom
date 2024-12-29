package com.shoppix.merchant_service_reactive.controller;

import com.shoppix.merchant_service_reactive.entity.MerchantDetails;
import com.shoppix.merchant_service_reactive.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@CrossOrigin("*")
@RestController
@RequestMapping("/merchant")
@Slf4j
public class MerchantController {

    @Autowired
    public MerchantService merchantService;

    private final Logger LOGGER = LoggerFactory.getLogger(MerchantController.class);

    @PostMapping("/registerNewMerchant")
    @ResponseBody
    public ResponseEntity<Mono<MerchantDetails>> registerNewMerchant(@RequestBody MerchantDetails merchantDetails) {

        LOGGER.info("Registering new merchant {}", merchantDetails);
        Mono<MerchantDetails> newMerchantDetails = merchantService.createOrUpdateMerchantDetails(merchantDetails);

        return new ResponseEntity<>(newMerchantDetails, HttpStatus.CREATED);
    }

    @GetMapping("/getMerchantDetailsById/{merchantId}")
    public ResponseEntity<Mono<MerchantDetails>> getMerchantDetailsByMerchantId(@PathVariable("merchantId") long merchantId) {

        Mono<MerchantDetails> merchantDetails = merchantService.getMerchantById(merchantId);

        return new ResponseEntity<>(merchantDetails, HttpStatus.OK);
    }

//    @PutMapping("/{merchantId}/addNewProductListing")
//    @ResponseBody
//    public ResponseEntity<Mono<MerchantDetails>> addNewProductListing(@PathVariable("merchantId") long merchantId,@RequestBody Product newProduct) {
//
//        Mono<MerchantDetails> updatedMerchantDetails = merchantService.addOrUpdateProductForMerchant(merchantId,newProduct);
//
//        return new ResponseEntity<>(updatedMerchantDetails, HttpStatus.OK);
//    }

}
