package com.meru.product_service.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.meru.product_service.entity.Product;
import com.meru.product_service.pojo.Inventory;
import com.meru.product_service.pojo.Promotions;
import com.meru.product_service.service.ProductService;

import javax.xml.ws.Response;

@CrossOrigin("*")
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
	@ResponseBody
	public ResponseEntity<?> saveProduct(@RequestBody Product prod){
		if(productService.getProductById(prod.getProductId()) == null)
			return new ResponseEntity(productService.saveProduct(prod),HttpStatus.OK);
		else
			return new ResponseEntity("PRODUCT ALREADY PRESENT, PLEASE UPDATE THE INVENTORY",HttpStatus.NOT_ACCEPTABLE);
	}

	@GetMapping("/getAll")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
	@Retry(name = PRODUCT_SERVICE)
	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<List<Product>> getAllProducts(){
		
		return new ResponseEntity(productService.getAllProducts(),HttpStatus.OK);
	}
	
	@GetMapping("/getProduct/{productId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productFallback")
//	@Retry(name = PRODUCT_SERVICE)
//	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<?> getProductById(@PathVariable("productId") int productId) {
		if(productService.getProductById(productId) != null)
			return new ResponseEntity(productService.getProductById(productId),HttpStatus.OK);
		else
			return new ResponseEntity("PRODUCT NOT AVAILABLE IN DATABASE",HttpStatus.NOT_FOUND);
	}
	
	@PutMapping("/updateProduct")
	@ResponseBody
	public ResponseEntity<Product> updateProduct(@RequestBody Product prod) {
		List<Product> products = new ArrayList<Product>();
		for(Product prod1 : products){
			if(prod1.getProductId()==prod.getProductId()){
				productService.saveProduct(prod);
			}
		}
		return new ResponseEntity(productService.saveProduct(prod),HttpStatus.OK);
	}
	
	@DeleteMapping("/delete/{productId}")
	public ResponseEntity<?> deleteProduct(@PathVariable("productId") int productId) {
		productService.deleteProductById(productId);
		return new ResponseEntity("PRODUCT ID "+productId+" SUCCESSFULLY DELETED",HttpStatus.INTERNAL_SERVER_ERROR);
		
	}

	/*FALLBACK METHOD FOR PRODUCT MICROSERVICE*/
	public ResponseEntity<?> productFallback(Exception e){
		return new ResponseEntity("WE ARE UNABLE TO FETCH YOUR PRODUCT DETAILS",HttpStatus.BAD_GATEWAY);
	}

	/* ALL MAPPINGS FOR PRODUCT-INVENTORY MICROSERVICE*/

	@PostMapping("/addInventory")
	@ResponseBody
	public ResponseEntity<Inventory> saveInventory(@RequestBody Inventory inv) {
		
		Inventory inv1 = restTemplate.postForObject("http://inventory-service/inventory/addInventory",inv, Inventory.class);

		int productId = inv1.getProductId();

		return new ResponseEntity(inv1,HttpStatus.OK);
	}
	
	@GetMapping("/inventory/{inventoryId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productInventoryFallback")
//	@Retry(name = PRODUCT_SERVICE)
//	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<Inventory> getProductByInventoryId(@PathVariable("inventoryId") int inventoryId) {
		
		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInv/"+inventoryId, Inventory.class);

		return new ResponseEntity(inv,HttpStatus.OK);
	}

	@PutMapping("/updateInventory/{productId}")
	@ResponseBody
	public ResponseEntity<Inventory> increaseProductQuantity(@PathVariable("productId") int productId, @RequestBody Inventory inv){

		Inventory inventory = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+productId,Inventory.class);
		inventory.setInventoryId(inventory.getInventoryId());
		inventory.setQuantity(inventory.getQuantity() == inv.getQuantity() ? inventory.getQuantity() : inv.getQuantity());
		inventory.setStatus(inventory.getStatus() ==  inv.getStatus() ? inventory.getStatus() : inv.getStatus());
		inventory.setProductId(inventory.getProductId() == inv.getProductId() ? inventory.getProductId() : inv.getProductId());

		Product prod = productService.getProductById(productId);
		prod.setStockStatus(inventory.getStatus());
		productService.updateProduct(prod,productId);
		restTemplate.put("http://inventory-service/inventory/updateInventory/"+inventory.getInventoryId(),inventory, Inventory.class);
		return new ResponseEntity(inv,HttpStatus.ACCEPTED);
	}
	
	@GetMapping("/getInventory/{productId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productInventoryFallback")
//	@Retry(name = PRODUCT_SERVICE)
//	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<Object> getInventoryByProductId(@PathVariable("productId") int productId) {
		
		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+productId,Inventory.class);
		
		Product prod=productService.getProductById(inv.getProductId());

		List<Object> prodInv = new ArrayList<>();
		prodInv.add(prod);
		prodInv.add(inv);

		return new ResponseEntity<Object>(prodInv,HttpStatus.OK);
		//return prod.toString()+" "+inv.toString();
	}

	/*FALLBACK METHOD FOR PRODUCT-INVENTORY MICROSERVICE*/
	public ResponseEntity<?> productInventoryFallback(Exception e){
		return new ResponseEntity("PRODUCT CURRENTLY UNAVAILABLE TEMPORARILY OR OUT OF STOCK !!",HttpStatus.SERVICE_UNAVAILABLE);
	}
	
	/* ALL MAPPINGS FOR PRODUCT - PROMOTIONS MICROSERVICE*/

	@PostMapping("/addPromo")
	@ResponseBody
	public ResponseEntity<?> savePromo(@RequestBody Promotions promo){
		Promotions promos = restTemplate.getForObject("http://promotions-service/promotions/getPromo/" +promo.getPromotionId(),Promotions.class);

		if(promos == null){
			Promotions promotion = new Promotions();
			promotion.setPromotionId(promo.getPromotionId());
			promotion.setOfferDetails(promo.getOfferDetails());
			promotion.setDiscountedPercentage(promo.getDiscountedPercentage());
			promotion.setExpiryDate(promo.getExpiryDate());
			restTemplate.postForObject("http://promotions-service/promotions/addPromo",promotion,Promotions.class);
			return new ResponseEntity<Promotions>(promotion,HttpStatus.OK);
		}else{
			return new ResponseEntity<Promotions>(HttpStatus.valueOf("PROMO ALREADY PRESENT"));
		}
	}

	@PostMapping("/{productId}/addPromosToProduct")
	@ResponseBody
	public ResponseEntity<?> addPromosToProduct(@PathVariable("productId") int productId,@RequestBody String promotionIds[]){

		Product product = productService.getProductById(productId);
		//product.setPromosAvailable(Arrays.asList(promotionIds));
		product.getPromosAvailable().addAll(Arrays.asList(promotionIds));
		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+productId,Inventory.class);
		restTemplate.delete("http://inventory-service/inventory/delete/"+inv.getInventoryId());
		productService.saveProduct(product);
		return new ResponseEntity("PROMOTIONS ADDED TO PRODUCT",HttpStatus.OK);
	}
//	@PostMapping("/addPromo")
//	@ResponseBody
//	public ResponseEntity<Promotions> savePromo(@RequestBody Promotions promo) {
//
//		//Promotions promos = restTemplate.postForObject("http://promotions-service/promotions/addPromo", promo, Promotions.class);
//
//		//Optional<Promotions> promos = Optional.ofNullable(restTemplate.getForObject("http://promotions-service/promotions/getPromo/"+promo.getProductId(),Promotions.class));
//
//		Product prod = getProductById(promo.getProductId());
//
//		if(prod != null){
//			Promotions promos = getProductByPromoId(promo.getPromotionId());
//			Promotions promo1 = null;
//			if(promos == null){
//				promo1 = new Promotions();
//				promo1.setPromotionId(promo.getPromotionId());
//				promo1.setOfferDetails(promo.getOfferDetails());
//				promo1.setDiscountedPercentage(promo.getDiscountedPercentage());
//				promo1.setExpiryDate(promo.getExpiryDate());
//
//				promo1.setProductId(promo.getProductId());
//				System.out.println("PROMOS FIRST IS "+promo1);
//				restTemplate.postForObject("http://promotions-service/promotions/addPromo",promo1,Promotions.class);
//			}
//			if(promos != null && !promos.getProductId().contains(promo.getProductId())){
//				promo1 = new Promotions();
//				promo1.setPromotionId(promo.getPromotionId());
//				promo1.setOfferDetails(promo.getOfferDetails());
//				promo1.setDiscountedPercentage(promo.getDiscountedPercentage());
//				promo1.setExpiryDate(promo.getExpiryDate());
//
//				promo1.setProductId(promos.getProductId().concat("," + promo.getProductId()));
//				System.out.println("PROMOS LAST IS "+promo1);
//				restTemplate.postForObject("http://promotions-service/promotions/addPromo",promo1,Promotions.class);
//			}
//			return new ResponseEntity<Promotions>(promo1, HttpStatus.OK);
//		} else{
//			return new ResponseEntity<Promotions>(HttpStatus.NOT_FOUND);
//		}
//	}
	
	@GetMapping("/byPromoId/{promoId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productPromotionFallback")
//	@Retry(name = PRODUCT_SERVICE)
//	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<Promotions> getProductByPromoId(@PathVariable("promoId") int promoId) {
		
		Promotions promos = restTemplate.getForObject("http://promotions-service/promotions/getPromo/"+promoId, Promotions.class);
		
		return new ResponseEntity(promos,HttpStatus.OK);
	}
	
	@GetMapping("/promoByProductId/{productId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productPromotionFallback")
//	@Retry(name = PRODUCT_SERVICE)
//	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<List<Promotions>> getAllPromosByProductId(@PathVariable("productId") int productId) {
		
		List<Promotions> promos = restTemplate.getForObject("http://promotions-service/promotions/getPromoByProduct/"+productId, List.class);

		Product prod = productService.getProductById(productId);
		return new ResponseEntity(promos,HttpStatus.OK);
		
	}
	
	/*COMPLETE PRODUCT DETAILS*/
	
	@GetMapping("/compProduct/{productId}")
	@CircuitBreaker(name = PRODUCT_SERVICE,fallbackMethod = "productPromotionFallback")
//	@Retry(name = PRODUCT_SERVICE)
//	@RateLimiter(name = PRODUCT_SERVICE)
	public ResponseEntity<CompleteProduct> compProduct(@PathVariable("productId") int productId) {
		
		Product prod = productService.getProductById(productId);
		
		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getProduct/"+productId,Inventory.class);
		
		List<Promotions> promos = restTemplate.getForObject("http://promotions-service/promotions/getPromoByProduct/"+productId, List.class);

		CompleteProduct completeProduct = new CompleteProduct(prod,inv, promos);
		return new ResponseEntity<CompleteProduct>(completeProduct,HttpStatus.OK);
		//return prod.toString()+ "\n" + inv.toString() + "\n" +promos.toString();
	}

	/*FALLBACK METHOD FOR PRODUCT-PROMOTIONS MICROSERVICE*/
	public ResponseEntity<?> productPromotionFallback(Exception e){
		return new ResponseEntity("PROMOS SEEMS TO BE NOT RESPONDING !!",HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
