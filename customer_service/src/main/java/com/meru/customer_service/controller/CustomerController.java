package com.meru.customer_service.controller;

import com.meru.customer_service.bean.Address;
import com.meru.customer_service.bean.Customer;
import com.meru.customer_service.exception.CustomerServiceException;
import com.meru.customer_service.model.*;
import com.meru.customer_service.service.CustomerService;
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

import java.util.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/customer")
@Slf4j
public class CustomerController {

	@Autowired
	public CustomerService customerService;

	@Autowired
	public RestTemplate restTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

	private static final String CUSTOMER_SERVICE = "customerService";

	private static final int DEFAULT_PRODUCT_QUANTITY = 8;


	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN
	 *
	 * METHOD TO REGISTER FIRST TIME NEW CUSTOMER INFORMATION
	 *
	 * @param customer
	 * @return
	 * @throws CustomerServiceException
	 */
	@PostMapping("/createCustomer")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@ResponseBody
	public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) throws CustomerServiceException {

		Optional<Customer> customerInfo = customerService.getCustomerByEmail(customer.getEmailId());

		if (customerInfo.isPresent()) {
			throw new CustomerServiceException("YOU HAVE ALREADY REGISTERED WITH US! ONE USER ONE EMAIL");
		} else {
			customerService.createCustomer(customer);
			return new ResponseEntity(customer, HttpStatus.OK);
		}
	}

	/**
	 * WILL BE CONTROLLED BY USER FIRST AND ADMIN LATER
	 *
	 * METHOD TO FETCH CUSTOMER INFORMATION BY EMAIL ADDRESS
	 *
	 * @param customerId
	 * @return
	 * @throws CustomerServiceException
	 */
	@GetMapping("/getCustomerById/{customerId}")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	public ResponseEntity<Customer> getCustomerByEmailId(@PathVariable("customerId") int customerId) throws CustomerServiceException {

		Optional<Customer> customerInfo = customerService.getCustomerById(customerId);
		if(customerInfo.isPresent()) {
			LOGGER.info("FETCHING CUSTOMER DETAILS BY CUSTOMER ID ["+ customerId+"]");
			return new ResponseEntity(customerInfo, HttpStatus.OK);
		}
		else
			throw new CustomerServiceException("CUSTOMER DETAILS NOT AVAILABLE");

	}

	/**
	 * WILL BE CONTROLLED BY USER FIRST AND ADMIN LATER
	 *
	 * METHOD TO FETCH CUSTOMER INFORMATION BY EMAIL ADDRESS
	 *
	 * @param emailId
	 * @return
	 * @throws CustomerServiceException
	 */
	@GetMapping("/getCustomerByEmailId/{emailId}")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	public ResponseEntity<Customer> getCustomerByEmailId(@PathVariable("emailId") String emailId) throws CustomerServiceException {

		Optional<Customer> customerInfo = customerService.getCustomerByEmail(emailId);
		if(customerInfo.isPresent()) {
			LOGGER.info("FETCHING CUSTOMER DETAILS BY CUSTOMER EMAIL ADDRESS ["+ emailId+"]");
			return new ResponseEntity(customerInfo, HttpStatus.OK);
		}
		else
			throw new CustomerServiceException("CUSTOMER DETAILS NOT AVAILABLE");

	}

	@GetMapping("/getDefaultAddress/{emailId}")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	public ResponseEntity<Optional<Address>> getDefaultAddressSelectedByCustomerEmailId(String emailId) throws CustomerServiceException {

			Optional<Address> defaultDeliveryAddress = customerService.getDefaultAddressSelectedByCustomerEmailId(emailId);
			if(defaultDeliveryAddress.isPresent()){
				LOGGER.info("FETCHED ADDRESS WITH CUSTOMER ID ["+emailId+"] SUCCESSFULLY");
				return new ResponseEntity(defaultDeliveryAddress, HttpStatus.OK);
			}
				throw new CustomerServiceException("ERROR FETCHING CUSTOMER ADDRESS WITH CUSTOMER EMAIL ID ["+emailId+"]");
	}
	/**
	 * WILL BE CONTROLLED BY ADMIN
	 *
	 * METHOD TO FETCH LIST OF CUSTOMERS REGISTERED WITH US
	 *
	 * @return
	 * @throws CustomerServiceException
	 */
	@GetMapping("/getAllCustomers")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	public ResponseEntity<List<Customer>> getAllCustomers() throws CustomerServiceException{

		List<Customer> allCustomerInfo = customerService.getAllCustomers();
		if(!allCustomerInfo.isEmpty()){
			LOGGER.info("FETCHED ALL CUSTOMERS INFO SUCCESSFULLY");
			return new ResponseEntity(allCustomerInfo,HttpStatus.OK);
		}else{
			throw new CustomerServiceException("ERROR FETCHING ALL CUSTOMER INFO");
		}
	}


	@PutMapping ("/updateOrderList")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@ResponseBody
	public ResponseEntity<List<Order>> updateOrderListOfCustomer(@RequestBody Order customerOrder) throws CustomerServiceException{

		if(customerOrder.getCustomerEmailId() == null){
			throw new CustomerServiceException("ERROR UPDATING ORDER LIST WITH CUSTOMER EMAIL ID");
		}else{
			List<Order> orderListUpdated = customerService.updateOrderList(customerOrder);
			return new ResponseEntity(orderListUpdated,HttpStatus.OK);
		}

	}
	/**
	 *
	 * @param emailId
	 * @return
	 * @throws CustomerServiceException
	 */
	@DeleteMapping("/deleteCustomer/{emailId}")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerFallback")
	public ResponseEntity<Boolean> deleteCustomerByIdOrEmail(@PathVariable String emailId) throws CustomerServiceException {

		if(customerService.deleteCustomerByEmail(emailId)){
			LOGGER.info("CUSTOMER DETAILS WITH EMAIL ID ["+emailId+"] DELETED SUCCESSFULLY");
			return new ResponseEntity("CUSTOMER DELETED",HttpStatus.ACCEPTED);
		}else{
			throw new CustomerServiceException("CAN'T DELETE ANYTHING? BECAUSE CUSTOMER DETAILS NOT AVAILABLE ["+emailId+"]");
		}
	}

	/*CUSTOMER-CART MICROSERVICE*/

	/**
	 *
	 * @param productId
	 * @param customerIdForCart
	 * @param cartProduct
	 * @return
	 */
	@PutMapping("/{customerIdForCart}/addProductToCart/{productId}")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerCartFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerCartFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerCartFallback")
	@ResponseBody
	public ResponseEntity<CartProducts> addProductToCart(@PathVariable int productId, @PathVariable int customerIdForCart, @RequestBody CartProducts cartProduct) throws CustomerServiceException {

		Cart cart = restTemplate.getForObject("http://cart-service/cart/viewCart/customer/"+customerIdForCart,Cart.class);
		if(cart != null){
			restTemplate.put("http://cart-service/cart/customer/"+customerIdForCart+"/addProductToCart/"+productId,cartProduct,CartProducts.class);
			LOGGER.info("PRODUCT ID "+productId+" ADDED TO CART SUCCESSFULLY "+cartProduct);
			return new ResponseEntity(cartProduct,HttpStatus.OK);
		}else{
			throw new CustomerServiceException("ERROR FETCHING CART AND PRODUCT DETAILS FOR CUSTOMER ID ["+customerIdForCart+"]");
		}
	}

	/**
	 *
	 * @param productId
	 * @param customerIdForCart
	 * @return
	 */
	@DeleteMapping("/{customerIdForCart}/deleteProductFromCart/{productId}")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerCartFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerCartFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerCartFallback")
	public ResponseEntity<Boolean> deleteProductFromCart(@PathVariable int productId, @PathVariable int customerIdForCart) throws CustomerServiceException {

		Cart cart = restTemplate.getForObject("http://cart-service/viewCart/customer/"+customerIdForCart, Cart.class);
		if(cart != null){
			Set<CartProducts> productsInCart = cart.getCartProducts();
			productsInCart.forEach(product -> {
				if (product.getProductId() == productId)
					productsInCart.remove(product.getProductId());
			});
			LOGGER.info("PRODUCTS DELETED FROM CART SUCCESSFULLY");
			return new ResponseEntity("PRODUCT DELETED SUCCESSFULLY", HttpStatus.OK);
		}else{
			throw new CustomerServiceException("ERROR DELETING PRODUCT ID ["+productId+"] | NO DATA AVAILABLE");
		}
	}

	/*CUSTOMER-ORDER MICROSERVICE*/

	/**
	 *
	 * @param orderRequest
	 * @return
	 */
	@PutMapping("/orderProduct")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerOrderFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerOrderFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerOrderFallback")
	@ResponseBody
	public ResponseEntity<OrderRequest> orderNow(@RequestBody OrderRequest orderRequest) throws CustomerServiceException {
		if(orderRequest != null){
			restTemplate.postForObject("http://checkout-service/checkout/proceedForPayment",orderRequest,OrderRequest.class);
			LOGGER.info("ORDER REQUEST RECEIVED"+orderRequest);
			return new ResponseEntity(orderRequest,HttpStatus.OK);
		}else{
			throw new CustomerServiceException("ERROR PLACING ORDER FOR CUSTOMER ID ["+orderRequest.getCustomerId()+"]");
		}
	}

	@GetMapping("/{emailId}/myOrders")
	@CircuitBreaker(name = CUSTOMER_SERVICE,fallbackMethod = "customerOrderFallback")
	@Retry(name = CUSTOMER_SERVICE,fallbackMethod = "customerOrderFallback")
	@RateLimiter(name = CUSTOMER_SERVICE,fallbackMethod = "customerOrderFallback")
	public ResponseEntity<List<Order>> myOrders(@PathVariable("emailId") String emailId){
		List<Order> myOrders = customerService.getAllOrdersForCustomer(emailId);
		if(!myOrders.isEmpty()){
			LOGGER.info("FETCHED ALL YOUR RECENT ORDERS");
			return new ResponseEntity(myOrders,HttpStatus.OK);
		}else{
			throw new CustomerServiceException("YOU HAVEN'T ORDERED ANYTHING RECENTLY ! PLEASE CONTINUE SHOPPING");
		}
	}

	/** FALLBACK METHODS FOR CUSTOMER, ORDER AND CART */

	public ResponseEntity<?> customerFallback(Exception e){
		return new ResponseEntity("ERROR, WE ARE UNABLE TO COMMUNICATE CUSTOMER SERVICE",HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public ResponseEntity<?> customerOrderFallback(Exception e){
		return new ResponseEntity("ERROR, WE ARE UNABLE TO COMMUNICATE WITH ORDER SERVICE",HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public ResponseEntity<?> customerCartFallback(Exception e){
		return new ResponseEntity("ERROR, WE ARE UNABLE TO COMMUNICATE WITH CART SERVICE",HttpStatus.INTERNAL_SERVER_ERROR);
	}
}