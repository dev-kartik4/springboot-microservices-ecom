package com.shoppix.inventory_service_reactive.service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.inventory_service_reactive.enums.InventoryEnum;
import com.shoppix.inventory_service_reactive.events.InventoryEvent;
import com.shoppix.inventory_service_reactive.exception.InventoryServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shoppix.inventory_service_reactive.entity.Inventory;
import com.shoppix.inventory_service_reactive.repo.InventoryRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class InventoryService {
	
	@Autowired
	public InventoryRepo invRepo;

	@Value("${spring.kafka.topic.inventory-topic}")
	private String inventoryTopic;

	private final ObjectMapper objectMapper;

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    private InventoryKafkaProducerService inventoryKafkaProducerService;

	@Autowired
    public InventoryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Inventory> saveOrUpdateInventory(Inventory inventory) throws InventoryServiceException {

		if (inventory.getInventoryId() != 0 && inventory.getInventoryId() > 0) {

			return invRepo.findById(inventory.getInventoryId())
					.flatMap(existingInventory -> {
						existingInventory.setInventoryId(inventory.getInventoryId());
						existingInventory.setInventoryCode(inventory.getInventoryCode());
						existingInventory.setSkuIds(inventory.getSkuIds());
						existingInventory.setProductId(inventory.getProductId());
						existingInventory.setProductName(inventory.getProductName());
						existingInventory.setProductBrand(inventory.getProductBrand());
						existingInventory.setProductPrice(inventory.getProductPrice());
						existingInventory.setModelNumber(inventory.getModelNumber());
						existingInventory.setProductSerialNumber(inventory.getProductSerialNumber());
						existingInventory.setCategory(inventory.getCategory());
						existingInventory.setProductFulfillmentChannel(inventory.getProductFulfillmentChannel());
						existingInventory.setAvailableQuantity(inventory.getAvailableQuantity());
						existingInventory.setReservedQuantity(inventory.getReservedQuantity());
						existingInventory.setReorderLevel(inventory.getReorderLevel());
						existingInventory.setReorderQuantity(inventory.getReorderQuantity());
						existingInventory.setStockAlertLevel(inventory.getStockAlertLevel());
						existingInventory.setStockStatus(inventory.getStockStatus());
						existingInventory.setStockType(inventory.getStockType());
						existingInventory.setDamagedQuantity(inventory.getDamagedQuantity());
						existingInventory.setReturnedQuantity(inventory.getReturnedQuantity());
						existingInventory.setMinOrderQuantity(inventory.getMinOrderQuantity());
						existingInventory.setStockHistoryWithDate(inventory.getStockHistoryWithDate());
						existingInventory.setProductSeller(inventory.getProductSeller());
						existingInventory.setProductSupplier(inventory.getProductSupplier());
						existingInventory.setStockAvailableLocation(inventory.getStockAvailableLocation());
						existingInventory.setEventStatus(InventoryEnum.INVENTORY_UPDATED.name());
						existingInventory.setFirstCreatedAt(inventory.getFirstCreatedAt());
						existingInventory.setLastUpdatedAt(generateLastUpdatedDateTime(new Date()));

						return invRepo.save(existingInventory).subscribeOn(Schedulers.parallel())
								.doOnSuccess(savedInventory -> {
									LOGGER.info("INVENTORY UPDATED SUCCESSFULLY");
									LOGGER.info("UPDATING PRODUCT AS INVENTORY AND STOCK DETAILS ARE UPDATED");
									updateProductWhenInventoryUpdated(existingInventory);
								})
								.onErrorResume(e -> {
									inventory.setEventStatus(InventoryEnum.INVENTORY_INIT_FAILED.name());
									inventory.setFirstCreatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
									deleteByInventoryId(inventory.getInventoryId());
									LOGGER.error("OOPS TECHNICAL ERROR! NEW INVENTORY ADDING PROCESS FAILED", e);
									return Mono.error(new InventoryServiceException("OOPS TECHNICAL ERROR! NEW INVENTORY ADDING PROCESS FAILED", e));
								});
					})
					.doOnNext(updatedInventory -> LOGGER.info("Updated Inventory: " + updatedInventory))
					.delaySubscription(Duration.ofMillis(3000));
		} else {

			return getAllInventoryDetails()
					.collectList()
					.flatMap(existingInventory -> {
						int totalInventorySize = existingInventory.size();
						inventory.setInventoryId(totalInventorySize + 1);
						inventory.setProductId(inventory.getProductId());
						inventory.setSkuIds(inventory.getSkuIds());
						inventory.setProductName(inventory.getProductName());
						inventory.setProductBrand(inventory.getProductBrand());
						inventory.setProductPrice(inventory.getProductPrice());
						inventory.setModelNumber(inventory.getModelNumber());
						inventory.setCategory(inventory.getCategory());
						String pattern = "PROD-".concat(inventory.getCategory().substring(0, 3)).concat("-");
						String productKey = generateUniqueID();
						inventory.setProductSerialNumber(pattern.concat(productKey).concat("-#"+inventory.getProductId()));
						inventory.setStockStatus(inventory.getStockStatus());
						inventory.setProductFulfillmentChannel(inventory.getProductFulfillmentChannel());
						inventory.setAvailableQuantity(inventory.getAvailableQuantity());
						inventory.setReservedQuantity(inventory.getReservedQuantity());
						inventory.setReorderLevel(inventory.getReorderLevel());
						inventory.setReorderQuantity(inventory.getReorderQuantity());
						inventory.setStockAlertLevel(inventory.getStockAlertLevel());
						inventory.setStockStatus(inventory.getStockStatus());
						inventory.setStockType(inventory.getStockType());
						inventory.setDamagedQuantity(inventory.getDamagedQuantity());
						inventory.setReturnedQuantity(inventory.getReturnedQuantity());
						inventory.setMinOrderQuantity(inventory.getMinOrderQuantity());
						inventory.setStockHistoryWithDate(inventory.getStockHistoryWithDate());
						inventory.setProductSeller(inventory.getProductSeller());
						inventory.setProductSupplier(inventory.getProductSupplier());
						inventory.setStockAvailableLocation(inventory.getStockAvailableLocation());
						inventory.setInventoryCode(generateInventoryCode(inventory.getInventoryId(),inventory.getStockAvailableLocation()));
						inventory.setEventStatus(InventoryEnum.INVENTORY_INIT_SUCCESS.name());
						inventory.setFirstCreatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
						inventory.setLastUpdatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));

						return invRepo.save(inventory).subscribeOn(Schedulers.parallel())
								.doOnSuccess(savedInventory -> {
									LOGGER.info("INVENTORY SAVED SUCCESSFULLY");
									LOGGER.info("SENDING CONFIRMATION EVENT AS INVENTORY IS SAVED");
									inventory.setEventStatus(InventoryEnum.INVENTORY_INIT_SUCCESS.name());
									inventory.setFirstCreatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
								}).doOnSuccess(savedInventory -> System.out.println("Saved Inventory: " + savedInventory))
								.onErrorResume(e -> {
									LOGGER.info("INVENTORY SAVING FAILED..");
									inventory.setEventStatus(InventoryEnum.INVENTORY_INIT_FAILED.name());
									inventory.setLastUpdatedAt(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
									deleteByInventoryId(inventory.getInventoryId());
									LOGGER.error("OOPS TECHNICAL ERROR! NEW INVENTORY ADDING PROCESS FAILED", e);
									return Mono.error(new InventoryServiceException("OOPS TECHNICAL ERROR! NEW INVENTORY ADDING PROCESS FAILED", e));
								});
					});
		}
	}

	private void updateProductWhenInventoryUpdated(Inventory existingInventory) {

		try{
			LOGGER.info("UPDATING PRODUCT THROUGH INVENTORY EVENT");
			InventoryEvent inventoryEvent = new InventoryEvent();
			inventoryEvent.setInventoryId(existingInventory.getInventoryId());
			inventoryEvent.setProductId(existingInventory.getProductId());
			inventoryEvent.setInventoryMessageType(existingInventory.getEventStatus());
			inventoryEvent.setInventory(existingInventory);

			String updatedInventoryAsMessage = objectMapper.writeValueAsString(inventoryEvent);
			inventoryKafkaProducerService.sendMessage(inventoryTopic,updatedInventoryAsMessage);
		} catch (JsonProcessingException e) {
			LOGGER.info("ERROR SENDING INVENTORY EVENT...");
            throw new RuntimeException(e);
        }
    }

	public Mono<Inventory> getInventoryById(int inventoryId) throws InventoryServiceException{

		Mono<Inventory> inventory = invRepo.findById(inventoryId);

		return inventory.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR WHILE FETCHING INVENTORY DETAILS FOR INVENTORY ID ["+inventoryId+"]");
			throw new InventoryServiceException("ERROR WHILE FETCHING INVENTORY DETAILS FOR INVENTORY ID ["+inventoryId+"]");
		})).doOnNext(System.out::println).delaySubscription(Duration.ofMillis(3000));
	}


	public Mono<Inventory> getInvByProductId(long productId) {
		return invRepo.findAll()
				.filter(inventory -> inventory.getProductId() == productId)
				.next()
				.switchIfEmpty(Mono.error(() -> {
					String errorMsg = "ERROR WHILE FETCHING INVENTORY DETAILS HAVING PRODUCT ID [" + productId + "]";
					LOGGER.error(errorMsg);
					return new InventoryServiceException(errorMsg);
				}))
				.doOnNext(inventory -> System.out.println("Found Inventory: " + inventory))
				.delaySubscription(Duration.ofMillis(3000));
	}


	public Flux<Inventory> getAllInventoryDetails() {
		Flux<Inventory> inventoryList = invRepo.findAll();

		return inventoryList
				.publishOn(Schedulers.parallel())  // Ensures the operation runs on a parallel scheduler
				.switchIfEmpty(Flux.empty())  // Returns an empty Flux if no data is found
				.doOnError(e -> LOGGER.error("ERROR FETCHING ENTIRE STOCK INVENTORY DETAILS", e))  // Log error if any occurs
				.delaySubscription(Duration.ofMillis(3000));  // Optional delay (for debugging or testing)
	}


	public AtomicBoolean deleteByInventoryId(int inventoryId) {

		LOGGER.info("DELETING... INVENTORY DETAILS WITH INVENTORY ID ["+inventoryId+"]");
		AtomicBoolean stockDetailsDeleted = new AtomicBoolean(false);
		Mono<Inventory> stockExists = getInventoryById(inventoryId);
		stockExists.publishOn(Schedulers.parallel()).filter(product -> {
			if((product != null)){
				invRepo.deleteById(inventoryId);
				stockDetailsDeleted.set(true);
				LOGGER.info("STOCK INVENTORY DETAILS DELETED WITH INVENTORY ID ["+inventoryId+"]");
			}
			return true;
		}).switchIfEmpty(Mono.error(() -> {
			LOGGER.error("ERROR WHILE DELETING STOCK INVENTORY DETAILS WITH INVENTORY ID ["+inventoryId+"] | NOT FOUND");
			throw new InventoryServiceException("ERROR WHILE DELETING STOCK INVENTORY DETAILS WITH INVENTORY ID ["+inventoryId+"] | NOT FOUND");
		})).doOnNext(System.out::println).delaySubscription(Duration.ofMillis(3000));

		return stockDetailsDeleted;
	}

	private String generateLastUpdatedDateTime(Date date) {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		return simpleDateFormat.format(date);
	}

	private String generateUniqueID() {

		return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
	}

	private String generateInventoryCode(int inventoryId,String stockAvailableLocation){

		return "INV-"+stockAvailableLocation+"-"+inventoryId;
	}

}
