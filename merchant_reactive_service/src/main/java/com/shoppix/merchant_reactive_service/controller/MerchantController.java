package com.shoppix.merchant_reactive_service.controller;

import com.shoppix.merchant_reactive_service.entity.MerchantDetails;
import com.shoppix.merchant_reactive_service.pojo.ResponseMessage;
import com.shoppix.merchant_reactive_service.service.MerchantService;
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
@RequestMapping("/api/v1/merchant")
@Slf4j
public class MerchantController {

    @Autowired
    public MerchantService merchantService;

    private final Logger LOGGER = LoggerFactory.getLogger(MerchantController.class);

    @PostMapping("/registerOrUpdateMerchant")
    @ResponseBody
    public ResponseEntity<Mono<MerchantDetails>> registerOrUpdateMerchant(@RequestBody MerchantDetails merchantDetails) {

        LOGGER.info("Registering new merchant {}", merchantDetails);
        Mono<MerchantDetails> newMerchantDetails = merchantService.createOrUpdateMerchantDetails(merchantDetails);

        return new ResponseEntity<>(newMerchantDetails, HttpStatus.CREATED);
    }

    @GetMapping("/getMerchantDetailsById/{merchantId}")
    public ResponseEntity<Mono<MerchantDetails>> getMerchantDetailsByMerchantId(@PathVariable("merchantId") long merchantId) {

        Mono<MerchantDetails> merchantDetails = merchantService.getMerchantById(merchantId);

        return new ResponseEntity<>(merchantDetails, HttpStatus.OK);
    }

    @DeleteMapping("/deleteMerchantAccount/{merchantId}")
    public Mono<ResponseEntity<ResponseMessage>> deleteMerchantAccount(@PathVariable("merchantId") long merchantId) {

        return merchantService.deleteByMerchantId(merchantId)
                .flatMap(deleted -> {
                    if(deleted) {
                        ResponseMessage responseMessage = new ResponseMessage();
                        responseMessage.setStatusCode(200);
                        responseMessage.setMessage("MERCHANT DELETED WITH ID [" + merchantId + "]");
                        return Mono.just(ResponseEntity.ok(responseMessage));
                    } else{
                        ResponseMessage responseMessage = new ResponseMessage();
                        responseMessage.setStatusCode(404);
                        responseMessage.setMessage("NO MERCHANT FOUND WITH ID [" + merchantId + "]");
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMessage));
                    }
                })
                .onErrorResume(e -> {
                    LOGGER.error("Error deleting merchant with ID [{}]", merchantId, e);
                    ResponseMessage responseMessage = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR DELETING MERCHANT WITH ID [" + merchantId + "]");
                    return Mono.just(ResponseEntity.internalServerError().body(responseMessage));
                });
    }
}
