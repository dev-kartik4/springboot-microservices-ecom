package com.shoppix.product_reactive_service.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.shoppix.product_reactive_service.exception.ProductServiceException;
import com.shoppix.product_reactive_service.pojo.Offers;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shoppix.product_reactive_service.entity.Product;
import com.shoppix.product_reactive_service.pojo.Inventory;
import com.shoppix.product_reactive_service.service.ProductService;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductController {
	
	@Autowired
	public ProductService productService;

	@Autowired
	public WebClient.Builder webClientBuilder;

	private final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	private static final String PRODUCT_SERVICE = "productService";
	
	/* ALL MAPPINGS FOR PRODUCT MICROSERVICE */

	/**
	 * WILL BE CONTROLLED BY SELLER AND ADMIN BOTH
	 * <p>
	 * API TO ADD NEW PRODUCT
	 *
	 * @param newProduct
	 * @return
	 * @throws ProductServiceException
	 */
	@PostMapping("/createNewProduct")
	@ResponseBody
	public ResponseEntity<Mono<Product>> createOrUpdateProduct(@RequestBody Product newProduct) throws ProductServiceException {

		LOGGER.info("ADDING NEW PRODUCT");
		Mono<Product> productData = productService.createOrUpdateProduct(newProduct);

		return new ResponseEntity(productData, HttpStatus.OK);
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * API TO FETCH ALL PRODUCTS
	 *
	 * @return
	 * @throws ProductServiceException
	 */
	@GetMapping("/getAllProducts")
	public ResponseEntity<Flux<Product>> getAllProducts() throws ProductServiceException{

		LOGGER.info("FETCHING ALL PRODUCTS THROUGH API CALL....");
		Flux<Product> allProductInfo = productService.getAllProducts();

		return new ResponseEntity<>(allProductInfo,HttpStatus.OK);
	}

	/**
	 * WILL BE CONTROLLED BY ADMIN
	 * <p>
	 * API TO PRODUCT BY PRODUCT ID
	 *
	 * @param productId
	 * @return
	 * @throws ProductServiceException
	 */
	@GetMapping("/filterProductById/{productId}")
	public ResponseEntity<Mono<Product>> filterProductById(@PathVariable("productId") int productId) throws ProductServiceException {

		LOGGER.info("FETCHING PRODUCT WITH PRODUCT ID ["+productId+"]");
		Mono<Product> product = productService.getProductById(productId);

		return new ResponseEntity(product,HttpStatus.OK);
	}



	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * API TO FETCH PRODUCT BY PRODUCT NAME
	 *
	 * @param productName
	 * @return
	 * @throws ProductServiceException
	 */
	@GetMapping("/filterProductsByName/{productName}")
	public ResponseEntity<?> filterProductByName(@PathVariable("productName") String productName) throws ProductServiceException {

		LOGGER.info("FETCHING PRODUCT WITH PRODUCT NAME ["+productName+"]");
		Mono<Product> product = productService.getProductByName(productName);

		return new ResponseEntity(product,HttpStatus.OK);
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * API TO FETCH PRODUCT BY PRODUCT NAME
	 *
	 * @param productName
	 * @return
	 * @throws ProductServiceException
	 */
	@GetMapping("/filterProductsByCategory/{productCategory}")
	public ResponseEntity<?> filterProductsByCategory(@PathVariable("productName") String productName) throws ProductServiceException {

		LOGGER.info("FETCHING PRODUCT WITH PRODUCT NAME ["+productName+"]");
		Mono<Product> product = productService.getProductByName(productName);

		return new ResponseEntity(product,HttpStatus.OK);
	}

	/**
	 * WILL BE CONTROLLED BY ADMIN
	 * @param productId
	 * @return
	 * @throws ProductServiceException
	 */
	@DeleteMapping("/deleteProductById/{productId}")
	public ResponseEntity<Mono<Boolean>> deleteProductById(@PathVariable("productId") int productId) throws ProductServiceException{

		AtomicBoolean productDeleted = productService.deleteProductById(productId);

		if(productDeleted.equals(true)){
			LOGGER.info("PRODUCT ID "+productId+" SUCCESSFULLY DELETED");
			return new ResponseEntity("PRODUCT ID ["+productId+"] SUCCESSFULLY DELETED ["+productDeleted+"]",HttpStatus.FORBIDDEN);
		}else{
			LOGGER.error("ERROR DELETING PRODUCT WITH PRODUCT ID ["+productId+"] | NOT FOUND");
			throw new ProductServiceException("ERROR DELETING PRODUCT WITH PRODUCT ID ["+productId+"] | NOT FOUND");
		}
	}

	/* ALL MAPPINGS FOR PRODUCT-INVENTORY MICROSERVICE*/

//	@PostMapping("/addNewStock")
//	@ResponseBody
//	public ResponseEntity<Mono<Inventory>> createOrUpdateInventory(@RequestBody Inventory inv) throws ProductServiceException{
//
//		LOGGER.info("ADDING NEW STOCK DETAILS FOR PRODUCT...");
//		Mono<Inventory> inventory = productService.createOrUpdateInventoryForProduct(inv.getProductId(),inv);
//
//		return new ResponseEntity(inventory,HttpStatus.OK);
//	}
	
	@GetMapping("/inventory/{inventoryId}")
	public ResponseEntity<Mono<Inventory>> getProductByInventoryId(@PathVariable("inventoryId") int inventoryId) throws ProductServiceException{

		Mono<Inventory> inventory = productService.getProductByInventoryId(inventoryId);

		return new ResponseEntity(inventory,HttpStatus.OK);
	}

/*	@PutMapping("/updateInventory/{productId}")
	@ResponseBody
	public ResponseEntity<Mono<Inventory>> increaseProductQuantity(@PathVariable("productId") int productId, @RequestBody Inventory inv) throws ProductServiceException {

		Mono<Inventory> inventory = productService.increaseProductQuantity(productId, inv);

		return new ResponseEntity<>(inventory, HttpStatus.OK);
	}
	
	*//* ALL MAPPINGS FOR PRODUCT - PROMOTIONS MICROSERVICE*//*

	@PostMapping("/addPromo")
	@ResponseBody
	public ResponseEntity<Mono<Offers>> savePromo(@RequestBody Offers promo) throws ProductServiceException{

		Mono<Offers> promotionalOffers = productService.savePromo(promo);

		return new ResponseEntity<>(promotionalOffers,HttpStatus.OK);
	}

	@PostMapping("/{productId}/addPromosToProduct")
	@ResponseBody
	public ResponseEntity<?> addPromosToProduct(@PathVariable("productId") int productId,@RequestBody List<Offers> promotionalOffers){

		Mono<Product> product = productService.addPromosToProduct(productId,promotionalOffers);

		return new ResponseEntity(product,HttpStatus.OK);
	}*/

//
//	@GetMapping("/byPromoId/{promoId}")
//	public ResponseEntity<Offers> getProductByPromoId(@PathVariable("promoId") int promoId) {
//
//		Offers promos = restTemplate.getForObject("http://promotions-service/promotions/getPromo/"+promoId, Offers.class);
//
//		return new ResponseEntity(promos,HttpStatus.OK);
//	}
//
//	@GetMapping("/promoByProductId/{productId}")
//	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productPromotionFallback")
//	@Retry(name = PRODUCT_SERVICE,fallbackMethod = "productPromotionFallback")
//	@RateLimiter(name = PRODUCT_SERVICE,fallbackMethod = "productPromotionFallback")
//	public ResponseEntity<List<Offers>> getAllPromosByProductId(@PathVariable("productId") int productId) {
//
//		List<Offers> promos = restTemplate.getForObject("http://promotions-service/promotions/getPromoByProduct/"+productId, List.class);
//
//		Product prod = productService.getProductById(productId);
//		return new ResponseEntity(promos,HttpStatus.OK);
//
//	}
//
//	/*COMPLETE PRODUCT DETAILS*/
//
//	@GetMapping("/compProduct/{productId}")
//	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productPromotionFallback")
//	@Retry(name = PRODUCT_SERVICE)
//	@RateLimiter(name = PRODUCT_SERVICE)
//	public ResponseEntity<?> compProduct(@PathVariable("productId") int productId) {
//
//		Product prod = productService.getProductById(productId);
//
//		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+productId,Inventory.class);
//
//		//Set<Offers> promos = restTemplate.getForObject("http://promotions-service/promotions/getPromoByProduct/"+productId, List.class);
//
//		CompleteProduct completeProduct = new CompleteProduct();
//		completeProduct.setProd(prod);
//		completeProduct.setInv(inv);
//		return new ResponseEntity(completeProduct,HttpStatus.OK);
//		//return prod.toString()+ "\n" + inv.toString() + "\n" +promos.toString();
//	}
}
