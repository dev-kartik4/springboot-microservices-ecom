package com.shoppix.product_reactive_service.service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.product_reactive_service.entity.*;
import com.shoppix.product_reactive_service.enums.InventoryEnum;
import com.shoppix.product_reactive_service.enums.ProductEnum;
import com.shoppix.product_reactive_service.events.InventoryEvent;
import com.shoppix.product_reactive_service.events.MerchantProductEvent;
import com.shoppix.product_reactive_service.exception.ProductServiceException;
import com.shoppix.product_reactive_service.pojo.Inventory;
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

	public static final String INVENTORY_SERVICE_URL = "http://inventory-service/inventory";

	public static final String OFFERS_SERVICE_URL = "http://offers-service/offers";

	/**
	 *
	 * @param product
	 * @return
	 * @throws ProductServiceException
	 */

	public Mono<Product> createOrUpdateProduct(Product product) {

		LOGGER.info("CREATING NEW PRODUCT IN PROGRESS");

		return productRepo.findByParentProductId(product.getParentProductId())
				.switchIfEmpty(Mono.defer(() -> createNewProduct(product)))
				.flatMap(existingProduct -> updateProduct(existingProduct, product));
	}


	private Mono<Product> createNewProduct(Product product) {

		LOGGER.info("CREATING NEW PRODUCT...");

		Product newProduct = new Product();
		if(product.getParentProductId() == null){
			newProduct.setParentProductId(sequenceGeneratorService.generateProductUniqueID(product.getCategory()));
		}else{
			newProduct.setParentProductId(product.getParentProductId());
		}
		newProduct.setProductName(product.getProductName());
		newProduct.setProductVariations(product.getProductVariations());
		newProduct.setCategory(product.getCategory());
		newProduct.setSubCategory(product.getSubCategory());
		newProduct.setProductVariations(product.getProductVariations());
		newProduct.setMerchantId(product.getMerchantId());
		newProduct.setMerchantSellingName(product.getMerchantSellingName());
		newProduct.setAvailablePincodesForProduct(product.getAvailablePincodesForProduct());
		newProduct.setEventStatus(ProductEnum.PRODUCT_CREATE_SUCCESS.name());
		newProduct.setCreatedDateTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
		newProduct.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));

		product.getProductVariations().forEach(variation -> {
			LOGGER.info("GENERATING SKU CODE AND PRODUCT VARIANT DETAILS");
			if(Boolean.TRUE.equals(Utils.isBlank(variation.getVariantProductId()))){
				variation.setVariantProductId(sequenceGeneratorService.generateProductUniqueID(product.getSubCategory()));
			}else{
				variation.setVariantProductId(variation.getVariantProductId());
			}
			if(variation.getSkuData().getSkuCode().isEmpty()){
				variation.getSkuData().setSkuCode(sequenceGeneratorService
						.generateSKUCode(product.getCategory(),product.getSubCategory(),variation.getBrand(),variation.getProductDescription().getColor(),  variation.getProductDescription().getSize()));
			}else{
				variation.getSkuData().setSkuCode(variation.getSkuData().getSkuCode());
			}
			variation.getSkuData().setQuantityOfStock(variation.getSkuData().getQuantityOfStock());
			variation.setBrand(variation.getBrand());
			variation.setProductAvailabilityStatus(variation.getProductAvailabilityStatus());
			variation.setAverageRating(variation.getAverageRating());
			variation.setProductImages(variation.getProductImages());
			variation.setOffersAvailable(variation.getOffersAvailable());
			variation.setRatingsAndReviews(variation.getRatingsAndReviews());
			variation.setProductDescription(variation.getProductDescription());
		});

		Inventory inventory = new Inventory();
		inventory.setParentProductId(newProduct.getParentProductId());
		inventory.setProductName(newProduct.getProductName());
		inventory.setMerchantId(newProduct.getMerchantId());
		inventory.setMerchantSellingName(newProduct.getMerchantSellingName());
		inventory.setCategory(newProduct.getCategory());
		inventory.setSubCategory(newProduct.getSubCategory());
		inventory.setProductVariants(newProduct.getProductVariations());

		if (product.getProductFulfillmentChannel().equals(ProductEnum.PRODUCT_FB_MERCHANT.name())) {
			inventory.setProductFulfillmentChannel(ProductEnum.PRODUCT_FB_MERCHANT.name());
			newProduct.setProductManufacturer(product.getProductManufacturer());
			inventory.setProductSeller(product.getProductSeller());
		} else if (product.getProductFulfillmentChannel().equals(ProductEnum.PRODUCT_FB_SHOPPIX.name())){
			inventory.setProductFulfillmentChannel(product.getProductFulfillmentChannel());
			newProduct.setProductManufacturer(product.getProductManufacturer());
			inventory.setProductSeller(product.getProductSeller());
		}

		return productRepo.insert(newProduct).subscribeOn(Schedulers.parallel())
				.doOnSuccess(savedProduct -> {
					LOGGER.info("PRODUCT CREATED SUCCESSFULLY");
					LOGGER.info("SENDING REQUEST FOR INITIATION OF NEW STOCK FOR PRODUCT ID [" + newProduct.getParentProductId() + "]");
					inventory.setEventStatus(InventoryEnum.PRODUCT_INVENTORY_INIT_IN_PROGRESS.name());
					inventory.setFirstCreatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
					createOrUpdateInventoryForProduct(newProduct.getParentProductId(), inventory);
				})
				.onErrorResume(e -> {
					inventory.setEventStatus(InventoryEnum.PRODUCT_INVENTORY_INIT_FAILED.name());
					inventory.setFirstCreatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
					deleteByParentProductId(newProduct.getParentProductId());
					LOGGER.error("OOPS TECHNICAL ERROR! NEW PRODUCT ADDING PROCESS FAILED", e);
					return Mono.error(new ProductServiceException("OOPS TECHNICAL ERROR! NEW PRODUCT ADDING PROCESS FAILED", e));
				});
	}



	private Mono<Product> updateProduct(Product existingProduct, Product updatedProduct) {

		LOGGER.info("UPDATING PRODUCT IN PROGRESS");

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
				.doOnSuccess(savedProduct -> LOGGER.info("PRODUCT UPDATED WITH PRODUCT ID: {}", savedProduct.getParentProductId()))
				.onErrorResume(e -> {
					LOGGER.error("FAILED TO UPDATE PRODUCT WITH PRODUCT ID: {}", updatedProduct.getParentProductId(), e);
					return Mono.error(new ProductServiceException("FAILED TO UPDATE PRODUCT WITH PRODUCT ID: " + updatedProduct.getParentProductId()));
				});
	}

	/**
	 *
	 * @return
	 * @throws ProductServiceException
	 */
	public Flux<Product> getAllProducts() throws ProductServiceException{

		LOGGER.info("FETCHING.... ALL THE PRODUCTS");
		Flux<Product> products = productRepo.findAll();
		return products.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR WHILE COMMUNICATING SERVICE AND FETCHING ALL PRODUCTS");
			throw new ProductServiceException("ERROR WHILE COMMUNICATING SERVICE AND FETCHING ALL PRODUCTS");
		})).doOnNext(System.out::println).delaySubscription(Duration.ofMillis(3000));
	}

	/**
	 *
	 * @param productId
	 * @return
	 * @throws ProductServiceException
	 */
	public Mono<Product> getProductById(String productId) throws ProductServiceException{

		Mono<Product> product = productRepo.findById(productId);
		return product.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.empty()).doOnNext(System.out::println).delaySubscription(Duration.ofMillis(3000));
	}

	/**
	 *
	 * @param productName
	 * @return
	 * @throws ProductServiceException
	 */
	public Mono<Product> getProductByName(String productName) throws ProductServiceException {

		Mono<Product> product = productRepo.findProductByProductName(productName);
		return product.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR WHILE FETCHING PRODUCT WITH PRODUCT NAME ["+productName+"]");
			throw new ProductServiceException("ERROR WHILE FETCHING PRODUCT WITH PRODUCT NAME ["+productName+"]");
		}));
	}

	public Mono<String> checkServiceablePincode(String productId, String pincode) throws ProductServiceException {

		return getProductById(productId).map(product -> product.getAvailablePincodesForProduct().contains(pincode) ?
				"Product Deliverable at - "+pincode : "Sorry, we cannot deliver this product in your area");
	}

	/**
	 *
	 * @param parentProductId
	 * @return
	 * @throws ProductServiceException
	 */
	public Mono<Boolean> deleteByParentProductId(String parentProductId) throws ProductServiceException {

		LOGGER.info("DELETING... PARENT PRODUCT WITH ID ["+parentProductId+"]");

		return productRepo.findById(parentProductId).hasElement().doOnSuccess(prodId -> {
			LOGGER.info("PRODUCT DELETED SUCCESSFULLY [" + parentProductId + "]");
			productRepo.deleteById(parentProductId).subscribe();
		}).switchIfEmpty(Mono.defer(() -> {
			LOGGER.info("NO PRODUCT FOUND WITH ID [" + parentProductId + "]");
			return Mono.just(false);
		}));
	}

	public void createOrUpdateInventoryForProduct(String productId,Inventory newInventory) throws ProductServiceException{

		LOGGER.info("NEW INVENTORY INITIATION IN PROGRESS FOR THE PRODUCT ID ["+productId+"]");
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

			Mono<Product> productDetails = getProductById(newInventory.getParentProductId()).publishOn(Schedulers.parallel());
			productDetails.flatMap(updatedProduct -> {
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

	public Mono<Inventory> getProductByInventoryId(int inventoryId) throws ProductServiceException{

		LOGGER.info("FETCHING PRODUCT DETAILS BY INVENTORY ID");
		Mono<Inventory> inventory = webClientBuilder.build()
				.get()
				.uri(INVENTORY_SERVICE_URL.concat("/getInv/").concat(String.valueOf(inventoryId)))
				.retrieve()
				.bodyToMono(Inventory.class)
				.publishOn(Schedulers.parallel());

		return inventory.switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR FETCHING PRODUCT DETAILS BY INVENTORY ID ["+inventoryId+"]");
			throw new ProductServiceException("ERROR FETCHING PRODUCT DETAILS BY INVENTORY ID ["+inventoryId+"]");
		})).delaySubscription(Duration.ofMillis(3000));
	}

	public void deleteProductsConnectedWithMerchantId(long merchantId) throws ProductServiceException{

		try{
			getAllProducts().toStream().filter(product -> product.getMerchantId() == merchantId).forEach(product -> {
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


}
