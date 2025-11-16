package com.shoppix.product_reactive_service.service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.product_reactive_service.entity.*;
import com.shoppix.product_reactive_service.enums.InventoryEnum;
import com.shoppix.product_reactive_service.enums.ProductEnum;
import com.shoppix.product_reactive_service.events.InventoryEvent;
import com.shoppix.product_reactive_service.exception.ProductServiceException;
import com.shoppix.product_reactive_service.pojo.Inventory;
import com.shoppix.product_reactive_service.pojo.ResponseMessage;
import com.shoppix.product_reactive_service.utility.ProductIdGenerator;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shoppix.product_reactive_service.repo.ProductRepo;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ProductService{

    @Autowired
    public ProductRepo productRepo;

    @Autowired
    public ProductIdGenerator productIdGenerator;

    @Autowired
    public WebClient.Builder webClientBuilder;

    @Autowired
    public SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    private ProductKafkaProducerService productKafkaProducerService;

    @Value("${spring.kafka.topic.product-topic}")
    private String productTopic;

    @Value("${spring.kafka.topic.inventory-topic}")
    private String inventoryTopic;

    @Autowired
    public ProductService(ProductRepo productRepo, SequenceGeneratorService sequenceGeneratorService, ObjectMapper objectMapper) {
        this.productRepo = productRepo;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.objectMapper = objectMapper;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private final ObjectMapper objectMapper;

    public static final String INVENTORY_SERVICE_URL = "http://inventory-reactive-service/api/v1/inventory";

    public static final String OFFERS_SERVICE_URL = "http://offers-reactive-service/api/v1/offers";

    /**
     *
     * @param product
     * @return
     * @throws ProductServiceException
     */

    public Mono<ResponseMessage> createOrUpdateProduct(Product product) {

        LOGGER.info("FETCHING PRODUCT EXISTENCE...");

        return productRepo.findByParentProductId(product.getParentProductId())
                        .flatMap(existingProduct -> updateProduct(existingProduct, product))
                        .switchIfEmpty(createNewProduct(product));
    }


    private Mono<ResponseMessage> createNewProduct(Product productRequest) {

        LOGGER.info("SETTING UP INFORMATION FOR NEW PRODUCT...");

        Product newProduct = prepareProduct(productRequest);
        Inventory inventory = prepareInventory(newProduct,productRequest);

        return productRepo.insert(newProduct)
                        .subscribeOn(Schedulers.parallel())
                        .log("CREATING NEW PRODUCT")
                        .flatMap(savedProduct -> {
                            inventory.setEventStatus(InventoryEnum.PRODUCT_INVENTORY_INIT_IN_PROGRESS.name());
                            inventory.setFirstCreatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
                            createOrUpdateInventoryForProduct(newProduct.getParentProductId(), inventory);
                            return buildResponse(200,"PRODUCT CREATED SUCCESSFULLY",LocalDateTime.now().toString(),savedProduct,new ArrayList<>(),"/product/createOrUpdateProduct");
                        })
                        .doOnSuccess(responseMessage -> LOGGER.info("PRODUCT CREATED SUCCESSFULLY"))
                        .log("SENDING REQUEST FOR INITIATION OF NEW STOCK FOR PRODUCT ID [" + newProduct.getParentProductId() + "]")
                        .doOnError(e -> LOGGER.error("OOPS TECHNICAL ERROR! NEW PRODUCT ADDING PROCESS FAILED", e))
                        .onErrorResume(e -> {
                            inventory.setEventStatus(InventoryEnum.PRODUCT_INVENTORY_INIT_FAILED.name());
                            inventory.setFirstCreatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
                            deleteByParentProductId(newProduct.getParentProductId()).subscribe();
                            return buildResponse(500,"OOPS TECHNICAL ERROR! NEW PRODUCT ADDING PROCESS FAILED",LocalDateTime.now().toString(),new Object(),List.of(e.getMessage()),"/product/createOrUpdateProduct");
                        });
    }

    private Product prepareProduct(Product productRequest) {

        Product newProduct = new Product();

        String parentProductId = productRequest.getParentProductId().isEmpty() ? sequenceGeneratorService.generateProductUniqueID(productRequest.getCategory()) : productRequest.getParentProductId();

        newProduct.setParentProductId(parentProductId);
        newProduct.setProductName(productRequest.getProductName());
        newProduct.setProductVariations(productRequest.getProductVariations());
        newProduct.setCategory(productRequest.getCategory());
        newProduct.setSubCategory(productRequest.getSubCategory());
        newProduct.setProductVariations(productRequest.getProductVariations());
        newProduct.setMerchantId(productRequest.getMerchantId());
        newProduct.setMerchantSellingName(productRequest.getMerchantSellingName());
        newProduct.setAvailablePincodesForProduct(productRequest.getAvailablePincodesForProduct());
        newProduct.setEventStatus(ProductEnum.PRODUCT_CREATE_SUCCESS.name());
        newProduct.setCreatedDateTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
        newProduct.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));

        productRequest.getProductVariations().forEach(productVariation -> prepareVariation(productRequest,productVariation));
        return newProduct;
    }

    private void prepareVariation(Product productRequest, ProductVariations variation) {

        LOGGER.info("GENERATING SKU CODE AND PRODUCT VARIANT DETAILS");

        String variantProductId = Utils.isBlank(variation.getVariantProductId()) ? sequenceGeneratorService.generateProductUniqueID(productRequest.getCategory()) : variation.getVariantProductId();

        variation.setVariantProductId(variantProductId);

        String variantSkuCode = Utils.isBlank(variation.getSkuData().getSkuCode()) ? sequenceGeneratorService
                .generateSKUCode(productRequest.getCategory(),productRequest.getSubCategory(),variation.getBrand(),variation.getProductDescription().getColor(),  variation.getProductDescription().getSize()) : variation.getSkuData().getSkuCode();

        variation.getSkuData().setSkuCode(variantSkuCode);
        variation.getSkuData().setQuantityOfStock(variation.getSkuData().getQuantityOfStock());
        variation.setBrand(variation.getBrand());
        variation.setProductAvailabilityStatus(variation.getProductAvailabilityStatus());
        variation.setAverageRating(variation.getAverageRating());
        variation.setProductImages(variation.getProductImages());
        variation.setOffersAvailable(variation.getOffersAvailable());
        variation.setRatingsAndReviews(variation.getRatingsAndReviews());
        variation.setProductDescription(variation.getProductDescription());

    }

    private Inventory prepareInventory(Product newProduct, Product productRequest) {

        Inventory inventory = new Inventory();
        inventory.setParentProductId(newProduct.getParentProductId());
        inventory.setProductName(newProduct.getProductName());
        inventory.setMerchantId(newProduct.getMerchantId());
        inventory.setMerchantSellingName(newProduct.getMerchantSellingName());
        inventory.setCategory(newProduct.getCategory());
        inventory.setSubCategory(newProduct.getSubCategory());
        inventory.setProductVariants(newProduct.getProductVariations());

        if (productRequest.getProductFulfillmentChannel().equals(ProductEnum.PRODUCT_FB_MERCHANT.name())) {
            inventory.setProductFulfillmentChannel(ProductEnum.PRODUCT_FB_MERCHANT.name());
            newProduct.setProductManufacturer(productRequest.getProductManufacturer());
            inventory.setProductSeller(productRequest.getProductSeller());
        } else if (productRequest.getProductFulfillmentChannel().equals(ProductEnum.PRODUCT_FB_SHOPPIX.name())){
            inventory.setProductFulfillmentChannel(productRequest.getProductFulfillmentChannel());
            newProduct.setProductManufacturer(productRequest.getProductManufacturer());
            inventory.setProductSeller(productRequest.getProductSeller());
        }
        return inventory;
    }

    private Mono<ResponseMessage> updateProduct(Product existingProduct, Product updatedProduct) {

        existingProduct.setParentProductId(existingProduct.getParentProductId());
        existingProduct.setProductName(updatedProduct.getProductName());
        existingProduct.setProductVariations(updatedProduct.getProductVariations());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setSubCategory(updatedProduct.getSubCategory());
        existingProduct.setProductFulfillmentChannel(updatedProduct.getProductFulfillmentChannel());
        existingProduct.setProductManufacturer(updatedProduct.getProductManufacturer());
        existingProduct.setProductSeller(updatedProduct.getProductSeller());
        existingProduct.setMerchantId(updatedProduct.getMerchantId());
        existingProduct.setMerchantSellingName(updatedProduct.getMerchantSellingName());
        existingProduct.setAvailablePincodesForProduct(updatedProduct.getAvailablePincodesForProduct());
        existingProduct.setEventStatus(ProductEnum.PRODUCT_UPDATED.name());
        existingProduct.setCreatedDateTime(existingProduct.getCreatedDateTime());
        existingProduct.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));

        return productRepo.save(existingProduct)
                        .log("UPDATING PRODUCT IN PROGRESS")
                        .flatMap(savedProduct -> buildResponse(200,"PRODUCT UPDATED SUCCESSFULLY",LocalDateTime.now().toString(),savedProduct,new ArrayList<>(),"/product/createOrUpdateProduct"))
                        .doOnSuccess(responseMessage -> LOGGER.info("PRODUCT UPDATED SUCCESSFULLY"))
                        .doOnError(e -> LOGGER.error("FAILED TO UPDATE PRODUCT WITH PRODUCT ID: ["+ updatedProduct.getParentProductId()+"]", e))
                        .onErrorResume(e -> buildResponse(500,"FAILED TO UPDATE PRODUCT WITH PRODUCT ID: ["+ updatedProduct.getParentProductId()+"]",LocalDateTime.now().toString(),new Object(),List.of(e.getMessage()),"/product/createOrUpdateProduct"));
    }

    /**
     *
     * @return
     * @throws ProductServiceException
     */
    public Mono<ResponseMessage> filterAllProducts() throws ProductServiceException{

        LOGGER.info("FETCHING.... ALL THE PRODUCTS");
        Flux<Product> allProducts = productRepo.findAll();

        return allProducts.collectList()
                        .publishOn(Schedulers.parallel())
                        .log("FETCHING.... ALL THE PRODUCTS")
                        .flatMap(products -> buildResponse(200,"ALL PRODUCTS FETCHED SUCCESSFULLY",LocalDateTime.now().toString(),products,new ArrayList<>(),"/products/filterAllProducts"))
                        .doOnSuccess(responseMessage -> LOGGER.info("ALL PRODUCTS FETCHED SUCCESSFULLY"))
                        .switchIfEmpty(Mono.defer(() -> buildResponse(404,"NO PRODUCTS FOUND",LocalDateTime.now().toString(),allProducts,new ArrayList<>(),"/products/filterAllProducts")))
                        .doOnError(e -> LOGGER.error("ERROR FETCHING ALL PRODUCT DETAILS"))
                        .onErrorResume(e -> buildResponse(500,"ERROR FETCHING ALL PRODUCT INFO",LocalDateTime.now().toString(),allProducts,List.of(e.getMessage()),"/product/filterAllProducts"));
    }

    /**
     *
     * @param parentProductId
     * @return
     * @throws ProductServiceException
     */
    public Mono<ResponseMessage> filterByParentProductId(String parentProductId) throws ProductServiceException{

        return productRepo.findById(parentProductId)
                        .publishOn(Schedulers.parallel())
                        .log("FETCHING.... PRODUCT BY PARENT PRODUCT ID")
                        .flatMap(prod -> buildResponse(200,"PARENT PRODUCT ID FETCHED [" +prod.getParentProductId()+ "]",LocalDateTime.now().toString(),prod,new ArrayList<>(),"/products/filterByParentProductId/"+parentProductId))
                        .doOnSuccess(responseMessage -> LOGGER.info("PARENT PRODUCT ID FETCH SUCCESSFUL FOR [" +parentProductId+ "]"))
                        .switchIfEmpty(Mono.defer(() -> buildResponse(404,"PARENT PRODUCT ID [" + parentProductId + "] DOES NOT EXIST",LocalDateTime.now().toString(),null,new ArrayList<>(),"/products/filterByParentProductId/"+parentProductId)))
                        .doOnError(e -> LOGGER.error("ERROR FETCHING PRODUCT DETAILS",e))
                        .onErrorResume(e -> buildResponse(500,"ERROR FETCHING PRODUCT DETAILS FOR PARENT PRODUCT ID [" + parentProductId + "]",LocalDateTime.now().toString(),null,List.of(e.getMessage()),"/products/filterByParentProductId/"+parentProductId));
    }

    /**
     *
     * @param productName
     * @return
     * @throws ProductServiceException
     */
    public Mono<ResponseMessage> filterByProductName(String productName) throws ProductServiceException {

        return productRepo.findProductByProductName(productName)
                        .publishOn(Schedulers.parallel())
                        .log("FETCHING.... PRODUCT BY PRODUCT NAME")
                        .flatMap(product -> buildResponse(200,"PRODUCT DETAILS WITH PRODUCT NAME FETCHED [" +productName+ "]",LocalDateTime.now().toString(),product,new ArrayList<>(),"/products/filterByProductName/"+productName))
                        .doOnSuccess(responseMessage -> LOGGER.info("PRODUCT DETAILS WITH PRODUCT NAME FETCHED SUCCESSFULLY"))
                        .switchIfEmpty(Mono.defer(() -> buildResponse(404,"PRODUCT WITH NAME [" + productName + "] DOES NOT EXIST",LocalDateTime.now().toString(),null,new ArrayList<>(),"/products/filterByProductName/"+productName)))
                        .doOnError(e -> LOGGER.error("ERROR WHILE FETCHING PRODUCT WITH PRODUCT NAME ["+productName+"]",e))
                        .onErrorResume(e -> buildResponse(500,"ERROR WHILE FETCHING PRODUCT WITH PRODUCT NAME ["+productName+"]",LocalDateTime.now().toString(),null,List.of(e.getMessage()),"/products/filterByProductName/"+productName));
}

public Mono<ResponseMessage> checkServiceablePincode(String productId, String pincode) throws ProductServiceException{

    return productRepo.findById(productId)
            .publishOn(Schedulers.parallel())
            .flatMap(product ->
                    product.getAvailablePincodesForProduct().contains(pincode) ?
                            buildResponse(200,"PRODUCT DELIVERABLE AT - "+pincode,LocalDateTime.now().toString(),"PRODUCT DELIVERABLE AT - "+pincode,new ArrayList<>(),"/checkPincode/"+productId+"/"+pincode)
                            :
                            buildResponse(404,"SORRY, WE CANNOT DELIVER THIS PRODUCT IN YOUR AREA WITH PINCODE ["+pincode+"]",LocalDateTime.now().toString(),"SORRY, WE CANNOT DELIVER THIS PRODUCT IN YOUR AREA WITH PINCODE ["+pincode+"]",new ArrayList<>(),"/checkPincode/"+productId+"/"+pincode))
            .doOnSuccess(responseMessage -> LOGGER.info("PRODUCT DELIVERABLE AT PINCODE - "+pincode))
            .doOnError(e -> LOGGER.error("ERROR FETCHING PINCODE DETAILS WITH PINCODE "+pincode,e))
            .onErrorResume(e -> buildResponse(500,"ERROR FETCHING PINCODE DETAILS WITH PINCODE ["+pincode+"]",LocalDateTime.now().toString(),"ERROR FETCHING PINCODE DETAILS WITH PINCODE ["+pincode+"]",List.of(e.getMessage()),"/checkPincode/"+productId+"/"+pincode));
}

/**
 *
 * @param parentProductId
 * @return
 * @throws ProductServiceException
 */
public Mono<ResponseMessage> deleteByParentProductId(String parentProductId) throws ProductServiceException {

    LOGGER.info("DELETING... PARENT PRODUCT WITH ID ["+parentProductId+"]");

    return productRepo.findById(parentProductId)
            .publishOn(Schedulers.parallel())
            .flatMap(product -> buildResponse(200,"PARENT PRODUCT ID DELETED SUCCESSFULLY [" +parentProductId+ "]",LocalDateTime.now().toString(),"PARENT PRODUCT ID DELETED SUCCESSFULLY [" + parentProductId + "]",new ArrayList<>(),"/deleteByParentProductId/"+parentProductId))
            .doOnSuccess(product -> {
                productRepo.deleteByParentProductId(parentProductId).subscribe();
                LOGGER.info("PRODUCT DELETED SUCCESSFULLY [" + parentProductId + "]");
            })
            .switchIfEmpty(Mono.defer(() -> buildResponse(404,"NO PRODUCT FOUND WITH ID [" + parentProductId + "]",LocalDateTime.now().toString(),"NO PRODUCT FOUND WITH ID [" + parentProductId + "]",new ArrayList<>(),"/deleteByParentProductId/"+parentProductId)))
            .doOnError(e -> LOGGER.error("ERROR DURING DELETING PRODUCT BY PARENT PRODUCT ID [" +parentProductId + "]",e))
            .onErrorResume(e -> buildResponse(500,"ERROR DURING DELETING PRODUCT BY PARENT PRODUCT ID [" +parentProductId + "]",LocalDateTime.now().toString(),"ERROR DURING DELETING PRODUCT BY PARENT PRODUCT ID [" +parentProductId + "]",List.of(e.getMessage()),"/deleteByParentProductId/"+parentProductId));
}

private void createOrUpdateInventoryForProduct(String productId,Inventory newInventory) throws ProductServiceException{

    LOGGER.info("INVENTORY CREATE OR UPDATE IN PROGRESS FOR THE PRODUCT ID ["+productId+"]");
    try{
        InventoryEvent inventoryEvent = new InventoryEvent();
        inventoryEvent.setInventory(newInventory);
        inventoryEvent.setInventoryMessageType(ProductEnum.PRODUCT_CREATE_SUCCESS.name());

        String inventoryAsMessage = objectMapper.writeValueAsString(inventoryEvent);

        productKafkaProducerService.sendMessage(inventoryTopic, inventoryAsMessage);
        LOGGER.info("PRODUCT CREATED AND INVENTORY DETAILS UPDATED");
    } catch (Exception e){
        deleteByParentProductId(productId).subscribe();
        LOGGER.error("ERROR DURING PROCESS OF ADDING NEW STOCK TO PRODUCT");
    }
}

public void updateProductWhenInventoryUpdated(Inventory newInventory) throws ProductServiceException{

    LOGGER.info("UPDATING PRODUCT BASED ON DATA FROM INVENTORY ");
    try{
        LOGGER.info("PRODUCT DETAILS UPDATING ...");

        productRepo.findByParentProductId(newInventory.getParentProductId()).flatMap(updatedProduct -> {
            updatedProduct.setParentProductId(newInventory.getParentProductId());
            updatedProduct.setProductName(newInventory.getProductName());
            updatedProduct.setCategory(newInventory.getCategory());
            updatedProduct.setSubCategory(newInventory.getSubCategory());
            updatedProduct.setProductFulfillmentChannel(newInventory.getProductFulfillmentChannel());
            updatedProduct.setProductManufacturer(newInventory.getProductSeller());
            updatedProduct.setProductSeller(newInventory.getProductSeller());
            updatedProduct.setProductVariations(newInventory.getProductVariants());
            return productRepo.save(updatedProduct);
        }).subscribe();
        LOGGER.info("PRODUCT DETAILS UPDATED");
    } catch (Exception e){
        LOGGER.error("ERROR DURING PROCESS OF UPDATING LATEST DETAILS OF PRODUCT");
    }
}

public Mono<ResponseMessage> getProductByInventoryId(int inventoryId) throws ProductServiceException{

    LOGGER.info("FETCHING PRODUCT DETAILS BY INVENTORY ID");
    Mono<Inventory> inventory = webClientBuilder.build()
            .get()
            .uri(INVENTORY_SERVICE_URL.concat("/getInv/").concat(String.valueOf(inventoryId)))
            .retrieve()
            .bodyToMono(Inventory.class)
            .publishOn(Schedulers.parallel());

    return inventory.publishOn(Schedulers.parallel())
            .log("FETCHING... PRODUCT DETAILS BY INVENTORY ID [" +inventoryId+ "]")
            .flatMap(inv -> buildResponse(200,"PRODUCT DETAILS FETCHED FOR INVENTORY ["+inventoryId+"]",LocalDateTime.now().toString(),inv,new ArrayList<>(),"/inventory/"+inventoryId))
            .doOnSuccess(responseMessage -> LOGGER.info("PRODUCT DETAILS FETCHED FOR INVENTORY ["+inventoryId+"]"))
            .switchIfEmpty(Mono.defer(() -> buildResponse(404,"PRODUCT DETAILS FETCHED FOR INVENTORY ["+inventoryId+"]",LocalDateTime.now().toString(),null,new ArrayList<>(),"/inventory/"+inventoryId)))
            .doOnError(e -> LOGGER.error("ERROR FETCHING PRODUCT DETAILS BY INVENTORY ID ["+inventoryId+"]"))
            .onErrorResume(e -> buildResponse(500,"ERROR FETCHING PRODUCT DETAILS BY INVENTORY ID ["+inventoryId+"]",LocalDateTime.now().toString(),null,List.of(e.getMessage()),"/inventory/"+inventoryId));
}

public void deleteProductsConnectedWithMerchantId(long merchantId) throws ProductServiceException{

    try{
        productRepo.findAll().toStream().filter(product -> product.getMerchantId() == merchantId).forEach(product -> {
            deleteByParentProductId(product.getParentProductId()).subscribe();
            LOGGER.info("PARENT PRODUCT ID ["+product.getParentProductId()+"] DELETED");
        });
    } catch (Exception e) {
        LOGGER.error("ERROR DURING DELETING PRODUCTS CONNECTED TO MERCHANT ID ["+merchantId+"]");
        throw new ProductServiceException("ERROR DURING DELETING PRODUCTS CONNECTED TO MERCHANT ID ["+merchantId+"]");
    }
}
//
//	public Mono<Inventory> increaseProductQuantity(int productId,Inventory inv) throws ProductServiceException{
//
//		Mono<Inventory> inventory = webClientBuilder.build()
//				.get()
//				.uri(INVENTORY_SERVICE_URL.concat("/getInventory/").concat(String.valueOf(productId)))
//				.retrieve()
//				.bodyToMono(Inventory.class)
//				.publishOn(Schedulers.parallel());
//
//		return inventory.map(inventoryStock -> {
//			inventoryStock.setInventoryId(inventoryStock.getInventoryId());
//			inventoryStock.setQuantity(inventoryStock.getQuantity() == inv.getQuantity() ? inventoryStock.getQuantity() : inv.getQuantity());
//			inventoryStock.setStatus(inventoryStock.getStatus() ==  inv.getStatus() ? inventoryStock.getStatus() : inv.getStatus());
//			inventoryStock.setProductId(inventoryStock.getProductId() == inv.getProductId() ? inventoryStock.getProductId() : productId);
//
//			Mono<Product> product = getProductById(productId);
//			product.map(prod -> {
//				prod.setStockStatus(inventoryStock.getStatus());
//				updateProduct(prod,productId);
//
//				return prod;
//			});
//
//			webClientBuilder.build()
//					.put()
//					.uri(INVENTORY_SERVICE_URL.concat("/updateInventory/").concat(String.valueOf(inventoryStock.getInventoryId())))
//					.bodyValue(inventoryStock)
//					.retrieve()
//					.toEntity(Inventory.class)
//					.publishOn(Schedulers.parallel());
//
//			LOGGER.info("PRODUCT STOCK STATUS UPDATED SUCCESSFULLY");
//
//			return inventoryStock;
//		}).switchIfEmpty(Mono.error(() -> {
//			LOGGER.error("ERROR UPDATING PRODUCT TO INCREASE QUANTITY FOR INVENTORY STOCK ID ["+inv.getInventoryId()+"]");
//			throw new ProductServiceException("RROR UPDATING PRODUCT TO INCREASE QUANTITY FOR INVENTORY STOCK ID ["+inv.getInventoryId()+"]");
//		})).delaySubscription(Duration.ofMillis(3000));
//	}
//
//	public Mono<Offers> savePromo(Offers promo){
//
//		Mono<Offers> productPromos = webClientBuilder.build()
//				.get()
//				.uri(PROMOTIONS_SERVICE_URL.concat("/getPromo/").concat(String.valueOf(promo.getPromotionId())))
//				.retrieve()
//				.bodyToMono(Offers.class)
//				.publishOn(Schedulers.parallel());
//
//		return (productPromos != null ? productPromos.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.defer(() -> {
//
//			Offers promotion = new Offers();
//			promotion.setPromotionId(promo.getPromotionId());
//			promotion.setOfferDetails(promo.getOfferDetails());
//			promotion.setDiscountedPercentage(promo.getDiscountedPercentage());
//			promotion.setExpiryDate(promo.getExpiryDate());
//
//			webClientBuilder.build()
//					.post()
//					.uri(PROMOTIONS_SERVICE_URL.concat("/addPromo"))
//					.bodyValue(promotion)
//					.retrieve()
//					.toEntity(Offers.class)
//					.thenReturn(new ResponseEntity(promotion, HttpStatus.CREATED));
//
//			LOGGER.info("ADDED NEW PROMO OFFER IN THE SALE");
//			return Mono.just(promotion);
//		})) : productPromos.delaySubscription(Duration.ofMillis(3000)).hasElement().then(Mono.error(() -> {
//			LOGGER.error("ERROR ADDING PROMO OFFER | ALREADY EXISTS !");
//			throw new ProductServiceException("ERROR ADDING PROMO OFFER | ALREADY EXISTS !");
//		})));
//	}
//
//	public Mono<Product> addPromosToProduct(int productId, List<Offers> promotionalOffers) {
//
//		Mono<Product> product = getProductById(productId);
//
//		product.map(prod -> {
//			prod.setPromosAvailable(promotionalOffers);
//			updateProduct(prod,productId);
//			return prod;
//		});
//
//		return product;
//	}

private String generateLastUpdatedDateTime(Date date) {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z");
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
    return simpleDateFormat.format(date);
}

private Mono<ResponseMessage> buildResponse(
        int statusCode,
        String message,
        String timeStamp,
        Object responseData,
        List<String> errorDetails,
        String path
) {
    return Mono.just(ResponseMessage.builder()
            .statusCode(statusCode)
            .message(message)
            .timestamp(timeStamp)
            .responseData(responseData)
            .errorDetails(errorDetails != null ? List.of(errorDetails.toString()) : null)
            .path(path)
            .build());
}

}

