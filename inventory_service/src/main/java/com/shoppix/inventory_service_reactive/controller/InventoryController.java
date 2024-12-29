package com.shoppix.inventory_service_reactive.controller;

import com.shoppix.inventory_service_reactive.exception.InventoryServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shoppix.inventory_service_reactive.entity.Inventory;
import com.shoppix.inventory_service_reactive.service.InventoryService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

@CrossOrigin("*")
@RestController
@RequestMapping("/inventory")
public class InventoryController {
	
	@Autowired
	public InventoryService inventoryService;

	private static final String INVENTORY_SERVICE = "inventoryService";

	private final Logger LOGGER = LoggerFactory.getLogger(InventoryController.class);
	
	@PostMapping("/addInventory")
//	@CircuitBreaker(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
//	@Retry(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
//	@RateLimiter(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@ResponseBody
	public ResponseEntity<Mono<Inventory>> saveInv(@RequestBody Inventory inv) {

		Mono<Inventory> inventory = inventoryService.saveOrUpdateInventory(inv);

		return new ResponseEntity<>(inventory, HttpStatus.OK);
	}
	
	@GetMapping("/getInventoryById/{inventoryId}")
	@CircuitBreaker(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@Retry(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@RateLimiter(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	public ResponseEntity<Mono<Inventory>> getByInventoryId(@PathVariable("inventoryId") int inventoryId) {

		Mono<Inventory> inventory = inventoryService.getInventoryById(inventoryId);

		return new ResponseEntity<>(inventory, HttpStatus.OK);
	}

	@GetMapping("/getAllInventoryDetails")
	@CircuitBreaker(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@Retry(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@RateLimiter(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	public ResponseEntity<Flux<Inventory>> getAllCustomers() throws InventoryServiceException {

		Flux<Inventory> allInventoryDetails = inventoryService.getAllInventoryDetails();

		return new ResponseEntity<>(allInventoryDetails,HttpStatus.OK);
	}
	
	@GetMapping("/getInventoryByProduct/{productId}")
	@CircuitBreaker(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@Retry(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@RateLimiter(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	public ResponseEntity<Mono<Inventory>> getInventoryByProductId(@PathVariable("productId") String parentProductId) {

		Mono<Inventory> inventory = inventoryService.getInvByProductId(parentProductId);

		return new ResponseEntity<>(inventory, HttpStatus.OK);
	}
	
	
	@PutMapping("/updateInventory")
	@CircuitBreaker(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@Retry(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@RateLimiter(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@ResponseBody
	public ResponseEntity<Mono<Inventory>> updateInv(@RequestBody Inventory inv) {

		Mono<Inventory> inventory = inventoryService.saveOrUpdateInventory(inv);

		return new ResponseEntity<>(inventory, HttpStatus.OK);
	}
	
	
	@DeleteMapping("/deleteInventory/{inventoryId}")
	@CircuitBreaker(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@Retry(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	@RateLimiter(name = INVENTORY_SERVICE,fallbackMethod = "inventoryFallback")
	public ResponseEntity<Mono<Boolean>> deleteById(@PathVariable("inventoryId") int inventoryId) {

		AtomicBoolean productDeleted = inventoryService.deleteByInventoryId(inventoryId);

		if(productDeleted.equals(true)){
			LOGGER.info("INVENTORY ID "+inventoryId+" SUCCESSFULLY DELETED");
			return new ResponseEntity("INVENTORY ID ["+inventoryId+"] SUCCESSFULLY DELETED",HttpStatus.FORBIDDEN);
		}else{
			LOGGER.error("ERROR DELETING INVENTORY WITH INVENTORY ID ["+inventoryId+"] | NOT FOUND");
			throw new InventoryServiceException("ERROR DELETING INVENTORY WITH INVENTORY ID ["+inventoryId+"] | NOT FOUND");
		}

	}

}
