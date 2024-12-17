package com.shoppix.product_reactive_service.service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.product_reactive_service.enums.InventoryEnum;
import com.shoppix.product_reactive_service.enums.ProductEnum;
import com.shoppix.product_reactive_service.events.InventoryEvent;
import com.shoppix.product_reactive_service.exception.ProductServiceException;
import com.shoppix.product_reactive_service.pojo.Inventory;
import com.shoppix.product_reactive_service.utility.ProductIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shoppix.product_reactive_service.entity.Product;
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

	public Mono<Product> createOrUpdateProduct(Product product){
		if(product.getProductId() == 0){
			return createNewProduct(product);
		}else{
			return updateProduct(product.getProductId(), product);
		}
	}

	private Mono<Product> createNewProduct(Product product) {

		return sequenceGeneratorService.generateNextSequence(Product.SEQUENCE_NAME)
				.flatMap(nextProductId -> {
					Product newProduct = new Product();
					newProduct.setProductId(nextProductId);
					newProduct.setProductName(product.getProductName());
					newProduct.setProductPrice(product.getProductPrice());
					newProduct.setModelNumber(product.getModelNumber());
					newProduct.setDiscountedPrice(product.getDiscountedPrice());
					if(product.getSkuIDs().isEmpty()){
						product.getProductDescription().stream().forEach(prodDesc -> {
							sequenceGeneratorService.generateSKU(product.getCategory(),product.getProductBrand(), prodDesc.getColor(),prodDesc.getSize());
						});
					}else{
						newProduct.setSkuIDs(product.getSkuIDs());
					}
					newProduct.setProductDescription(product.getProductDescription());
					newProduct.setCategory(product.getCategory());
					newProduct.setProductAvailabilityStatus(product.getProductAvailabilityStatus());
					newProduct.setAverageRating(product.getAverageRating());
					newProduct.setOffersAvailable(product.getOffersAvailable());
					newProduct.setProductImages(product.getProductImages());
					newProduct.setRatingsAndReviews(product.getRatingsAndReviews());
					newProduct.setEventStatus(ProductEnum.PRODUCT_CREATE_SUCCESS.name());
					newProduct.setCreatedDateTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
					newProduct.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));

					Inventory inventory = new Inventory();
					if(!product.getProductSeller().isEmpty()) {
						if (product.getProductFulfillmentChannel().equals(ProductEnum.PRODUCT_FB_SHOPPIX.name())) {
							inventory.setProductId(nextProductId);
							inventory.setSkuIds(product.getSkuIDs());
							inventory.setProductName(product.getProductName());
							inventory.setProductBrand(product.getProductBrand());
							inventory.setProductPrice(product.getProductPrice());
							inventory.setModelNumber(product.getModelNumber());
							inventory.setCategory(product.getCategory());
							inventory.setProductFulfillmentChannel(ProductEnum.PRODUCT_FB_SHOPPIX.name());

							newProduct.setProductFulfillmentChannel(product.getProductFulfillmentChannel());
							newProduct.setProductSeller(product.getProductSeller());
							inventory.setProductSeller(product.getProductSeller());
						} else if (product.getProductFulfillmentChannel().equals(ProductEnum.PRODUCT_FB_MERCHANT.name())) {
							inventory.setProductId(nextProductId);
							inventory.setSkuIds(product.getSkuIDs());
							inventory.setProductName(product.getProductName());
							inventory.setProductBrand(product.getProductBrand());
							inventory.setProductPrice(product.getProductPrice());
							inventory.setModelNumber(product.getModelNumber());
							inventory.setCategory(product.getCategory());
							inventory.setProductFulfillmentChannel(ProductEnum.PRODUCT_FB_MERCHANT.name());

							newProduct.setProductFulfillmentChannel(product.getProductFulfillmentChannel());
							newProduct.setProductSeller(product.getMerchantDetails().getMerchantName());
							inventory.setProductSeller(product.getProductSeller());
						} else if (product.getProductFulfillmentChannel().equals(ProductEnum.PRODUCT_FB_VENDOR.name())) {
							inventory.setProductId(nextProductId);
							inventory.setSkuIds(product.getSkuIDs());
							inventory.setProductName(product.getProductName());
							inventory.setProductBrand(product.getProductBrand());
							inventory.setProductPrice(product.getProductPrice());
							inventory.setModelNumber(product.getModelNumber());
							inventory.setCategory(product.getCategory());
							inventory.setProductFulfillmentChannel(ProductEnum.PRODUCT_FB_VENDOR.name());

							newProduct.setProductFulfillmentChannel(product.getProductFulfillmentChannel());
							newProduct.setProductSeller(product.getProductSeller());
							inventory.setProductSeller(product.getProductSeller());
						}
					}

					return productRepo.insert(newProduct).subscribeOn(Schedulers.parallel())
							.doOnSuccess(savedProduct -> {
								LOGGER.info("PRODUCT CREATED SUCCESSFULLY");
								LOGGER.info("SENDING REQUEST FOR INITIATION OF NEW STOCK FOR PRODUCT ID ["+newProduct.getProductId()+"]");
								inventory.setEventStatus(InventoryEnum.INVENTORY_INIT_IN_PROGRESS.name());
								inventory.setFirstCreatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
								createOrUpdateInventoryForProduct(newProduct.getProductId(),inventory);
							})
							.onErrorResume(e -> {
								inventory.setEventStatus(InventoryEnum.INVENTORY_INIT_FAILED.name());
								inventory.setFirstCreatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
								deleteProductById(newProduct.getProductId());
								LOGGER.error("OOPS TECHNICAL ERROR! NEW PRODUCT ADDING PROCESS FAILED", e);
								return Mono.error(new ProductServiceException("OOPS TECHNICAL ERROR! NEW PRODUCT ADDING PROCESS FAILED", e));
							});
				});
	}

	private Mono<Product> updateProduct(long productId,Product updatedProduct) {

		return productRepo.findById((int) productId)
				.flatMap(existingProduct -> {
					existingProduct.setProductId(updatedProduct.getProductId());
					existingProduct.setSkuIDs(updatedProduct.getSkuIDs());
					existingProduct.setProductName(updatedProduct.getProductName());
					existingProduct.setProductBrand(updatedProduct.getProductBrand());
					existingProduct.setProductPrice(updatedProduct.getProductPrice());
					existingProduct.setModelNumber(updatedProduct.getModelNumber());
					existingProduct.setDiscountedPrice(updatedProduct.getDiscountedPrice());
					existingProduct.setCategory(updatedProduct.getCategory());
					existingProduct.setProductAvailabilityStatus(updatedProduct.getProductAvailabilityStatus());
					existingProduct.setProductFulfillmentChannel(updatedProduct.getProductFulfillmentChannel());
					existingProduct.setProductImages(updatedProduct.getProductImages());
					existingProduct.setOffersAvailable(updatedProduct.getOffersAvailable());
					existingProduct.setProductDescription(updatedProduct.getProductDescription());
					existingProduct.setProductSeller(updatedProduct.getProductSeller());
					existingProduct.setAverageRating(updatedProduct.getAverageRating());
					existingProduct.setRatingsAndReviews(updatedProduct.getRatingsAndReviews());
					existingProduct.setEventStatus(updatedProduct.getEventStatus());
					existingProduct.setCreatedDateTime(updatedProduct.getCreatedDateTime());
					existingProduct.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));

					return productRepo.save(existingProduct)
							.doOnSuccess(savedProduct -> LOGGER.info("PRODUCT UPDATED WITH PRODUCT ID: {}", savedProduct.getProductId()))
							.onErrorResume(e -> {
								LOGGER.error("FAILED TO UPDATE PRODUCT WITH PRODUCT ID: {}", updatedProduct.getProductId(), e);
								return Mono.error(new ProductServiceException("FAILED TO UPDATE PRODUCT WITH PRODUCT ID: " + updatedProduct.getProductId()));
							});
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
	public Mono<Product> getProductById(long productId) throws ProductServiceException{

		Mono<Product> product = productRepo.findById((int) productId);
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

	/**
	 *
	 * @param productId
	 * @return
	 * @throws ProductServiceException
	 */
	public AtomicBoolean deleteProductById(long productId) throws ProductServiceException {

		LOGGER.info("DELETING... PRODUCT DETAILS WITH PRODUCT ID ["+productId+"]");
		AtomicBoolean productDeleted = new AtomicBoolean(false);
		Mono<Product> productExists = getProductById(productId);
		productExists.publishOn(Schedulers.parallel()).filter(product -> {
			if((product != null)){
				productRepo.deleteById((int) productId).subscribe();
				productDeleted.set(true);
				LOGGER.info("PRODUCT AND INVENTORY DETAILS DELETED WITH PRODUCT ID ["+productId+"]");
			}
			return true;
		}).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR WHILE DELETING PRODUCT WITH PRODUCT ID ["+productId+"] | NOT FOUND");
			throw new ProductServiceException("ERROR WHILE DELETING PRODUCT WITH PRODUCT ID ["+productId+"] | NOT FOUND");
		})).doOnNext(System.out::println).delaySubscription(Duration.ofMillis(3000));

		return productDeleted;
	}

	public void createOrUpdateInventoryForProduct(long productId,Inventory newInventory) throws ProductServiceException{

		LOGGER.info("NEW INVENTORY INITIATION IN PROGRESS FOR THE PRODUCT ID ["+productId+"]");
		try{
			InventoryEvent inventoryEvent = new InventoryEvent();
			inventoryEvent.setInventory(newInventory);
			inventoryEvent.setInventoryMessageType(ProductEnum.PRODUCT_CREATE_SUCCESS.name());

			String inventoryAsMessage = objectMapper.writeValueAsString(inventoryEvent);

			productKafkaProducerService.sendMessage(inventoryTopic, inventoryAsMessage);
			LOGGER.info("PRODUCT CREATED AND INVENTORY DETAILS UPDATED");
		} catch (Exception e){
			deleteProductById(productId);
			LOGGER.error("ERROR DURING PROCESS OF ADDING NEW STOCK TO PRODUCT");
		}
	}

	public void updateProductWhenInventoryUpdated(Inventory newInventory) throws ProductServiceException{

		LOGGER.info("UPDATING PRODUCT BASED ON DATA FROM INVENTORY ");
		try{
			LOGGER.info("PRODUCT DETAILS UPDATING ...");

			Mono<Product> productDetails = getProductById(newInventory.getProductId()).publishOn(Schedulers.parallel());
			productDetails.flatMap(updatedProduct -> {
				updatedProduct.setProductName(newInventory.getProductName());
				updatedProduct.setProductBrand(newInventory.getProductBrand());
				updatedProduct.setProductPrice(newInventory.getProductPrice());
				updatedProduct.setProductSerialNumber(newInventory.getProductSerialNumber());
				updatedProduct.setCategory(newInventory.getCategory());
				updatedProduct.setProductFulfillmentChannel(newInventory.getProductFulfillmentChannel());
				updatedProduct.setProductAvailabilityStatus(newInventory.getStockStatus());
				updatedProduct.setSkuIDs(newInventory.getSkuIds());
				updatedProduct.setProductSeller(newInventory.getProductSeller());
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
