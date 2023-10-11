package com.meru.product_service.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.meru.product_service.exception.ProductServiceException;
import com.meru.product_service.pojo.CompleteProduct;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.meru.product_service.entity.Product;
import com.meru.product_service.pojo.Inventory;
import com.meru.product_service.pojo.Promotions;
import com.meru.product_service.service.ProductService;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductController {
	
	@Autowired
	public ProductService productService;
	
	@Autowired
	public RestTemplate restTemplate;

	private final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	private static final String PRODUCT_SERVICE = "productService";
	
	/* ALL MAPPINGS FOR PRODUCT MICROSERVICE*/

	@PostMapping("/addProduct")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@Retry(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@RateLimiter(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@ResponseBody
	public ResponseEntity<Product> saveProduct(@RequestBody Product prod) throws ProductServiceException {
		if(productService.getProductByName(prod.getProductName()) == null)
			return new ResponseEntity(productService.saveProduct(prod),HttpStatus.OK);
		else
			throw new ProductServiceException("PRODUCT ALREADY PRESENT, PLEASE UPDATE THE INVENTORY");
	}

	@GetMapping("/getAll")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@Retry(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@RateLimiter(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	public ResponseEntity<List<Product>> getAllProducts() throws ProductServiceException{
		List<Product> allProductsList = new ArrayList<>();
		allProductsList = productService.getAllProducts();
		if(!allProductsList.isEmpty()){
			LOGGER.info("FETCHING LIST OF PRODUCT DETAILS");
			return new ResponseEntity(productService.getAllProducts(),HttpStatus.OK);
		}else{
			LOGGER.error("ERROR WHILE COMMUNICATING SERVICE AND FETCHING ALL PRODUCTS");
			throw new ProductServiceException("ERROR WHILE COMMUNICATING SERVICE AND FETCHING ALL PRODUCTS");
		}
	}
	
	@GetMapping("/getProduct/{productId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@Retry(name = PRODUCT_SERVICE)
	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<?> getProductById(@PathVariable("productId") int productId) throws ProductServiceException {
		if(productService.getProductById(productId) != null){
			LOGGER.info("FETCHING PRODUCT WITH PRODUCT ID ["+productId+"]");
			return new ResponseEntity(productService.getProductById(productId),HttpStatus.OK);
		}else{
			LOGGER.error("ERROR WHILE FETCHING PRODUCT WITH PRODUCT ID ["+productId+"]");
			throw new ProductServiceException("ERROR WHILE FETCHING PRODUCT WITH PRODUCT ID ["+productId+"]");
		}

	}


	@PutMapping("/updateProduct")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@Retry(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@RateLimiter(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@ResponseBody
	public ResponseEntity<Product> updateProduct(@RequestBody Product prod) throws ProductServiceException{
		List<Product> products = new ArrayList<Product>();
		for(Product prod1 : products){
			if(prod1.getProductId()==prod.getProductId()){
				productService.saveProduct(prod);
			}
		}
		return new ResponseEntity(productService.saveProduct(prod),HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteProduct/{productId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@Retry(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@RateLimiter(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	public ResponseEntity<String> deleteProduct(@PathVariable("productId") int productId) throws ProductServiceException{
		if(productService.deleteProductById(productId).equals("SUCCESS")){
			LOGGER.info("PRODUCT ID "+productId+" SUCCESSFULLY DELETED");
			return new ResponseEntity("PRODUCT ID "+productId+" SUCCESSFULLY DELETED",HttpStatus.INTERNAL_SERVER_ERROR);
		}else{
			LOGGER.error("ERROR DELETING PRODUCT WITH PRODUCT ID ["+productId+"] | NOT FOUND");
			throw new ProductServiceException("ERROR DELETING PRODUCT WITH PRODUCT ID ["+productId+"] | NOT FOUND");
		}
	}

	/*FALLBACK METHOD FOR PRODUCT MICROSERVICE*/
	public ResponseEntity<?> productFallback(Exception e){
		return new ResponseEntity("ERROR, WE ARE UNABLE TO FETCH YOUR PRODUCT DETAILS",HttpStatus.BAD_GATEWAY);
	}

	/* ALL MAPPINGS FOR PRODUCT-INVENTORY MICROSERVICE*/

	@PostMapping("/addInventory")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@Retry(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@RateLimiter(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@ResponseBody
	public ResponseEntity<Inventory> saveInventory(@RequestBody Inventory inv) throws ProductServiceException{
		
		Inventory inv1 = restTemplate.postForObject("http://inventory-service/inventory/addInventory",inv, Inventory.class);
		LOGGER.info("UPDATING INVENTORY DETAILS FOR PRODUCT WITH PRODUCT ID ["+inv.getProductId()+"]");

		return new ResponseEntity(inv1,HttpStatus.OK);
	}
	
	@GetMapping("/inventory/{inventoryId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productInventoryFallback")
	@Retry(name = PRODUCT_SERVICE)
	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<Inventory> getProductByInventoryId(@PathVariable("inventoryId") int inventoryId) throws ProductServiceException{
		
		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInv/"+inventoryId, Inventory.class);
		if(inv != null){
			LOGGER.info("FETCHING PRODUCT DETAILS BY INVENTORY ID");
			return new ResponseEntity(inv,HttpStatus.OK);
		}else{
			LOGGER.error("ERROR FETCHING PRODUCT DETAILS BY INVENTORY ID ["+inventoryId+"]");
			throw new ProductServiceException("ERROR FETCHING PRODUCT DETAILS BY INVENTORY ID ["+inventoryId+"]");
		}

	}

	@PutMapping("/updateInventory/{productId}")
	@ResponseBody
	public ResponseEntity<Inventory> increaseProductQuantity(@PathVariable("productId") int productId, @RequestBody Inventory inv) throws ProductServiceException{

		Inventory inventory = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+productId,Inventory.class);
		inventory.setInventoryId(inventory.getInventoryId());
		inventory.setQuantity(inventory.getQuantity() == inv.getQuantity() ? inventory.getQuantity() : inv.getQuantity());
		inventory.setStatus(inventory.getStatus() ==  inv.getStatus() ? inventory.getStatus() : inv.getStatus());
		inventory.setProductId(inventory.getProductId() == inv.getProductId() ? inventory.getProductId() : productId);

		Product prod = productService.getProductById(productId);
		prod.setStockStatus(inventory.getStatus());
		productService.updateProduct(prod,productId);
		restTemplate.put("http://inventory-service/inventory/updateInventory/"+inventory.getInventoryId(),inventory, Inventory.class);
		LOGGER.info("PRODUCT STOCK STATUS UPDATED SUCCESSFULLY");
		return new ResponseEntity(inventory,HttpStatus.ACCEPTED);
	}
	
	@GetMapping("/getInventory/{productId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productInventoryFallback")
	@Retry(name = PRODUCT_SERVICE)
	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<Object> getInventoryByProductId(@PathVariable("productId") int productId) {
		
		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+productId,Inventory.class);
		
		Product prod=productService.getProductById(inv.getProductId());

		List<Object> prodInv = new ArrayList<>();
		prodInv.add(prod);
		prodInv.add(inv);

		return new ResponseEntity<Object>(prodInv,HttpStatus.OK);
	}

	/*FALLBACK METHOD FOR PRODUCT-INVENTORY MICROSERVICE*/
	public ResponseEntity<?> productInventoryFallback(Exception e){
		return new ResponseEntity("PRODUCT CURRENTLY UNAVAILABLE TEMPORARILY OR OUT OF STOCK !!",HttpStatus.SERVICE_UNAVAILABLE);
	}
	
	/* ALL MAPPINGS FOR PRODUCT - PROMOTIONS MICROSERVICE*/

	@PostMapping("/addPromo")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@Retry(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@RateLimiter(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@ResponseBody
	public ResponseEntity<?> savePromo(@RequestBody Promotions promo) throws ProductServiceException{
		Promotions promos = restTemplate.getForObject("http://promotions-service/promotions/getPromo/" +promo.getPromotionId(),Promotions.class);

		if(promos == null){
			Promotions promotion = new Promotions();
			promotion.setPromotionId(promo.getPromotionId());
			promotion.setOfferDetails(promo.getOfferDetails());
			promotion.setDiscountedPercentage(promo.getDiscountedPercentage());
			promotion.setExpiryDate(promo.getExpiryDate());
			restTemplate.postForObject("http://promotions-service/promotions/addPromo",promotion,Promotions.class);
			LOGGER.info("ADDED NEW PROMO OFFER IN THE SALE");
			return new ResponseEntity<Promotions>(promotion,HttpStatus.OK);
		}else{
			LOGGER.error("ERROR ADDING PROMO OFFER | ALREADY EXISTS !");
			return new ResponseEntity<Promotions>(HttpStatus.valueOf("ERROR ADDING PROMO OFFER | ALREADY EXISTS !"));
		}
	}

	@PostMapping("/{productId}/addPromosToProduct")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@Retry(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@RateLimiter(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@ResponseBody
	public ResponseEntity<?> addPromosToProduct(@PathVariable("productId") int productId,@RequestBody String promotionIds[]){

		Product product = productService.getProductById(productId);
		product.getPromosAvailable().addAll(Arrays.asList(promotionIds));
		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+productId,Inventory.class);
		productService.updateProduct(product,productId);
		return new ResponseEntity("PROMOTIONS ADDED TO PRODUCT",HttpStatus.OK);
	}


	@GetMapping("/byPromoId/{promoId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productPromotionFallback")
	@Retry(name = PRODUCT_SERVICE)
	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<Promotions> getProductByPromoId(@PathVariable("promoId") int promoId) {
		
		Promotions promos = restTemplate.getForObject("http://promotions-service/promotions/getPromo/"+promoId, Promotions.class);
		
		return new ResponseEntity(promos,HttpStatus.OK);
	}
	
	@GetMapping("/promoByProductId/{productId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productPromotionFallback")
	@Retry(name = PRODUCT_SERVICE)
	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<List<Promotions>> getAllPromosByProductId(@PathVariable("productId") int productId) {
		
		List<Promotions> promos = restTemplate.getForObject("http://promotions-service/promotions/getPromoByProduct/"+productId, List.class);

		Product prod = productService.getProductById(productId);
		return new ResponseEntity(promos,HttpStatus.OK);
		
	}
	
	/*COMPLETE PRODUCT DETAILS*/
	
	@GetMapping("/compProduct/{productId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productPromotionFallback")
	@Retry(name = PRODUCT_SERVICE)
	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<?> compProduct(@PathVariable("productId") int productId) {
		
		Product prod = productService.getProductById(productId);
		
		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+productId,Inventory.class);
		
		//Set<Promotions> promos = restTemplate.getForObject("http://promotions-service/promotions/getPromoByProduct/"+productId, List.class);

		CompleteProduct completeProduct = new CompleteProduct();
		completeProduct.setProd(prod);
		completeProduct.setInv(inv);
		return new ResponseEntity(completeProduct,HttpStatus.OK);
		//return prod.toString()+ "\n" + inv.toString() + "\n" +promos.toString();
	}

	/*FALLBACK METHOD FOR PRODUCT-PROMOTIONS MICROSERVICE*/
	public ResponseEntity<?> productPromotionFallback(Exception e){
		return new ResponseEntity("PROMOS SEEMS TO BE NOT RESPONDING !!",HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
