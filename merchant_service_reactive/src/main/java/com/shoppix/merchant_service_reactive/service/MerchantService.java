package com.shoppix.merchant_service_reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.merchant_service_reactive.entity.MerchantProducts;
import com.shoppix.merchant_service_reactive.enums.MerchantEnum;
import com.shoppix.merchant_service_reactive.entity.MerchantDetails;
import com.shoppix.merchant_service_reactive.enums.MerchantProductEnum;
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
    private String productTopicName;

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

    private MerchantProducts populateMerchantProductData(long merchantId,MerchantProducts existingMerchantProducts) {

        MerchantProducts merchantProduct = new MerchantProducts();
        merchantProduct.setMerchantId(merchantId);
        merchantProduct.setMerchantSellingName(existingMerchantProducts.getMerchantSellingName());
        merchantProduct.setParentProductId(existingMerchantProducts.getParentProductId());
        merchantProduct.setProductName(existingMerchantProducts.getProductName());
        merchantProduct.setInventoryId(existingMerchantProducts.getInventoryId());
        merchantProduct.setInventoryCode(existingMerchantProducts.getInventoryCode());

        // Populate ProductVariations
//        List<MerchantProducts> populatedProductVariations = product.getProductVariations().stream()
//                .map(this::populateProductVariationData)
//                .collect(Collectors.toList());
//        merchantProduct.setProductVariations(populatedProductVariations);
//
//        // Populate other fields
//        merchantProduct.setCategory(product.getCategory());
//        merchantProduct.setSubCategory(product.getSubCategory());
//        merchantProduct.setProductFulfillmentChannel(product.getProductFulfillmentChannel());
//        merchantProduct.setProductManufacturer(product.getProductManufacturer());
//        merchantProduct.setProductSeller(product.getProductSeller());
//        merchantProduct.setAvailablePincodesForProduct(product.getAvailablePincodesForProduct());

        return merchantProduct;
    }

    public Mono<MerchantDetails> getMerchantById(long merchantId) throws MerchantServiceException{

        LOGGER.info("FETCHING MERCHANT DETAILS WITH MERCHANT ID ["+merchantId+"]...");

        Mono<MerchantDetails> merchantDetails = merchantRepo.findById(merchantId);

        return merchantDetails.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.empty());
    }

    public Mono<MerchantDetails> getMerchantByEmailAddress(String emailAddress) throws MerchantServiceException{

        LOGGER.info("FETCHING MERCHANT DETAILS WITH MERCHANT EMAIL ADDRESS ["+emailAddress+"]...");

        Mono<MerchantDetails> merchantDetails = merchantRepo.findByEmailId(emailAddress);

        return merchantDetails.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.empty());
    }

    public Mono<MerchantDetails> getMerchantByMerchantSellerName(String merchantSellerName) throws MerchantServiceException{

        LOGGER.info("FETCHING MERCHANT DETAILS WITH MERCHANT SELLER NAME ["+merchantSellerName+"]...");

        Mono<MerchantDetails> merchantDetails = merchantRepo.findByMerchantSellerName(merchantSellerName);

        return merchantDetails.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.empty());
    }



    public Mono<Boolean> deleteByMerchantId(long merchantId) {

        LOGGER.info("IN PROCESS OF DELETING MERCHANT WITH MERCHANT ID [" + merchantId + "]");

        return merchantRepo.deleteById(merchantId)
                .then(Mono.just(true))
                .onErrorResume(e -> {
                    LOGGER.error("Error during merchant deletion process", e);
                    return Mono.just(false);
                });
    }

    private void sendEventToProduct(MerchantDetails merchantDetails) throws JsonProcessingException {

        try{
            LOGGER.info("CREATING NEW PRODUCT BY MERCHANT...");
            MerchantProductEvent merchantProductEvent = new MerchantProductEvent();
            merchantProductEvent.setMerchantId(merchantDetails.getMerchantId());
            merchantProductEvent.setMerchantMessageType(MerchantProductEnum.MERCHANT_PRODUCT_CREATE.name());
            //merchantProductEvent.setMerchantProducts(merchantDetails.getListOfProductsByMerchant());

            String merchantProductAsMessage = objectMapper.writeValueAsString(merchantProductEvent);
            merchantKafkaProducerService.sendMessage(productTopicName,merchantProductAsMessage);
            LOGGER.info("PRODUCT CREATED BY MERCHANT. WILL BE DISPLAYED SHORTLY");
        } catch (Exception e) {
            LOGGER.error("ERROR DURING PROCESS OF CREATING NEW PRODUCT BY MERCHANT", e);
            throw new RuntimeException(e);
        }

    }

    private String generateLastUpdatedDateTime(Date date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        return simpleDateFormat.format(date);
    }

}
