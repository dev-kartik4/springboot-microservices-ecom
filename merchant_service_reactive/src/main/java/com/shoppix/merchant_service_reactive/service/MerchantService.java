package com.shoppix.merchant_service_reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.merchant_service_reactive.entity.MerchantProduct;
import com.shoppix.merchant_service_reactive.enums.MerchantEnum;
import com.shoppix.merchant_service_reactive.entity.MerchantDetails;
import com.shoppix.merchant_service_reactive.enums.MerchantProductEnum;
import com.shoppix.merchant_service_reactive.events.MerchantEvent;
import com.shoppix.merchant_service_reactive.events.MerchantProductEvent;
import com.shoppix.merchant_service_reactive.exception.MerchantServiceException;
import com.shoppix.merchant_service_reactive.repo.MerchantRepo;
import com.shoppix.merchant_service_reactive.util.MerchantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
public class MerchantService {

    @Autowired
    public MerchantRepo merchantRepo;

    @Autowired
    public MerchantKafkaProducerService merchantKafkaProducerService;

    @Autowired
    public MerchantUtil merchantUtil;

    @Autowired
    public WebClient.Builder webClientBuilder;

    @Value("${spring.kafka.topic.merchant-topic}")
    private String merchantTopic;

    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantService.class);

    @Autowired
    public MerchantService(ObjectMapper objectMapper) {
        this.merchantKafkaProducerService = merchantKafkaProducerService;
        this.objectMapper = objectMapper;
    }

    public Mono<MerchantDetails> createOrUpdateMerchantDetails(MerchantDetails merchantDetails){

        LOGGER.info("MERCHANT DETAILS CREATE OR UPDATE IN PROGRESS");

        return merchantRepo.findByMerchantSellerName(merchantDetails.getMerchantSellingName())
                .switchIfEmpty(Mono.defer(() -> createNewMerchant(merchantDetails)))
                .flatMap(existingMerchant -> updateExistingMerchant(existingMerchant, merchantDetails));
    }

    private Mono<MerchantDetails> createNewMerchant(MerchantDetails merchantDetails) {
        LOGGER.info("MERCHANT REGISTRATION IN PROGRESS");

        MerchantDetails newMerchantDetails = new MerchantDetails();
        newMerchantDetails.setMerchantId(merchantUtil.generateMerchantId());
        newMerchantDetails.setMerchantPersonName(merchantDetails.getMerchantPersonName());
        newMerchantDetails.setMerchantSellingName(merchantDetails.getMerchantSellingName());
        newMerchantDetails.setMerchantEmailAddress(merchantDetails.getMerchantEmailAddress());
        newMerchantDetails.setMerchantContactNumber(merchantDetails.getMerchantContactNumber());
        newMerchantDetails.setMerchantFullAddress(merchantDetails.getMerchantFullAddress());
        newMerchantDetails.setMerchantBankAccountNumber(merchantDetails.getMerchantBankAccountNumber());
        newMerchantDetails.setMerchantBankingName(merchantDetails.getMerchantBankingName());
        newMerchantDetails.setMerchantBankIfscCode(merchantDetails.getMerchantBankIfscCode());
        newMerchantDetails.setGstInTaxInformation(merchantDetails.getGstInTaxInformation());
        newMerchantDetails.setEventStatus(MerchantEnum.MERCHANT_REGISTERED.name());
        newMerchantDetails.setCreatedDateTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
        newMerchantDetails.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
        newMerchantDetails.setMerchantExistence(true);
        newMerchantDetails.setListOfProductsByMerchant(new ArrayList<>());

        return merchantRepo.insert(newMerchantDetails).subscribeOn(Schedulers.parallel())
                .doOnSuccess(savedMerchant -> System.out.println("MERCHANT CREATED SUCCESSFULLY " + savedMerchant))
                .onErrorResume(e -> {
                    LOGGER.error("MERCHANT CREATION FAILED");
                    newMerchantDetails.setEventStatus(MerchantEnum.MERCHANT_DELETED.name());
                    newMerchantDetails.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
                    deleteByMerchantId(merchantDetails.getMerchantId()).subscribe();
                    LOGGER.error("OOPS TECHNICAL ERROR! NEW MERCHANT CREATING PROCESS FAILED", e);
                    return Mono.error(new MerchantServiceException("OOPS TECHNICAL ERROR! NEW MERCHANT CREATING PROCESS FAILED", e));
                });
    }

    private Mono<MerchantDetails> updateExistingMerchant(MerchantDetails existingMerchant, MerchantDetails updatedMerchantDetails) {
        LOGGER.info("UPDATING EXISTING MERCHANT...");

        existingMerchant.setMerchantId(existingMerchant.getMerchantId());
        existingMerchant.setMerchantPersonName(updatedMerchantDetails.getMerchantPersonName());
        existingMerchant.setMerchantSellingName(updatedMerchantDetails.getMerchantSellingName());
        existingMerchant.setMerchantEmailAddress(updatedMerchantDetails.getMerchantEmailAddress());
        existingMerchant.setMerchantContactNumber(updatedMerchantDetails.getMerchantContactNumber());
        existingMerchant.setMerchantFullAddress(updatedMerchantDetails.getMerchantFullAddress());
        existingMerchant.setMerchantBankAccountNumber(updatedMerchantDetails.getMerchantBankAccountNumber());
        existingMerchant.setMerchantBankingName(updatedMerchantDetails.getMerchantBankingName());
        existingMerchant.setMerchantBankIfscCode(updatedMerchantDetails.getMerchantBankIfscCode());
        existingMerchant.setGstInTaxInformation(updatedMerchantDetails.getGstInTaxInformation());
        existingMerchant.setEventStatus(MerchantEnum.MERCHANT_UPDATED.name());
        existingMerchant.setCreatedDateTime(existingMerchant.getCreatedDateTime());
        existingMerchant.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
        existingMerchant.setMerchantExistence(true);

        updatedMerchantDetails.getListOfProductsByMerchant().forEach(product -> {
            if(!existingMerchant.getListOfProductsByMerchant().contains(product)) {
                existingMerchant.getListOfProductsByMerchant().add(populateMerchantProductData(existingMerchant.getMerchantId(),product));
            } else{
                existingMerchant.getListOfProductsByMerchant().remove(product);
                existingMerchant.getListOfProductsByMerchant().add(populateMerchantProductData(existingMerchant.getMerchantId(),product));
            }
        });

        return merchantRepo.save(existingMerchant)
                .doOnSuccess(savedMerchant -> LOGGER.info("MERCHANT UPDATED SUCCESSFULLY"))
                .onErrorResume(e -> {
                    LOGGER.error("FAILED TO UPDATE MERCHANT", e);
                    return Mono.error(new MerchantServiceException("FAILED TO UPDATE MERCHANT", e));
                });
    }

    private MerchantProduct populateMerchantProductData(long merchantId, MerchantProduct existingMerchantProduct) {

        MerchantProduct merchantProduct = new MerchantProduct();
        merchantProduct.setMerchantId(merchantId);
        merchantProduct.setMerchantSellingName(existingMerchantProduct.getMerchantSellingName());
        merchantProduct.setParentProductId(existingMerchantProduct.getParentProductId());
        merchantProduct.setProductName(existingMerchantProduct.getProductName());
        merchantProduct.setInventoryId(existingMerchantProduct.getInventoryId());
        merchantProduct.setInventoryCode(existingMerchantProduct.getInventoryCode());

        return merchantProduct;
    }

    public Mono<MerchantDetails> getMerchantById(long merchantId) throws MerchantServiceException{

        LOGGER.info("FETCHING MERCHANT DETAILS WITH MERCHANT ID ["+merchantId+"]...");

        Mono<MerchantDetails> merchantDetails = merchantRepo.findById(merchantId);

        return merchantDetails.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.empty());
    }

    public Mono<MerchantDetails> getMerchantByMerchantSellerName(String merchantSellerName) throws MerchantServiceException{

        LOGGER.info("FETCHING MERCHANT DETAILS WITH MERCHANT SELLER NAME ["+merchantSellerName+"]...");

        Mono<MerchantDetails> merchantDetails = merchantRepo.findByMerchantSellerName(merchantSellerName);

        return merchantDetails.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.empty());
    }


    public Mono<Boolean> deleteByMerchantId(long merchantId) {

        LOGGER.info("IN PROCESS OF DELETING MERCHANT WITH MERCHANT ID [" + merchantId + "]");

        return merchantRepo.findById(merchantId).hasElement().doOnSuccess(merchId -> {
            LOGGER.info("MERCHANT DELETED SUCCESSFULLY [" + merchantId + "]");
            deleteEntireProductAndInventoryConnectedWithMerchant(merchantId).subscribe();
            merchantRepo.deleteById(merchantId).subscribe();
        }).switchIfEmpty(Mono.defer(() -> {
            LOGGER.info("NO MERCHANT FOUND WITH ID [" + merchantId + "]");
            return Mono.just(false);
        }));
    }

    private Mono<Void> deleteEntireProductAndInventoryConnectedWithMerchant(long merchantId) {

        LOGGER.info("DELETING ENTIRE PRODUCT AND INVENTORY CONNECTED WITH MERCHANT ID...["+merchantId+"]");

        return getMerchantById(merchantId).flatMap(merchData -> {
            MerchantEvent merchantEvent = new MerchantEvent();
            merchantEvent.setMerchantId(merchantId);
            merchantEvent.setMerchantMessageType(MerchantEnum.MERCHANT_DELETED.name());
            merchantEvent.setMerchantDetails(merchData);

            LOGGER.info("MERCHANT EVENT: {}", merchantEvent);
            String merchantAsMessage;
            try{
                merchantAsMessage = objectMapper.writeValueAsString(merchantEvent);
            } catch (JsonProcessingException e) {
                LOGGER.error("Error serializing merchant event: ", e);
                return Mono.error(new RuntimeException("Error serializing merchant event", e));
            }
            return Mono.fromRunnable(() -> {
                merchantKafkaProducerService.sendMessage(merchantTopic,merchantAsMessage);
                LOGGER.info("PRODUCT AND INVENTORY DELETED BY MERCHANT AND WILL GET DE-SCOPED SHORTLY");
                LOGGER.info("MERCHANT DELETED SUCCESSFULLY [" + merchantId + "]");
            }).then();
        }).switchIfEmpty(Mono.defer(() -> {
            LOGGER.warn("Merchant with ID {} not found", merchantId);
            return Mono.empty(); // Return Mono.empty() if merchant not found
        }));
    }

    private String generateLastUpdatedDateTime(Date date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        return simpleDateFormat.format(date);
    }

}
