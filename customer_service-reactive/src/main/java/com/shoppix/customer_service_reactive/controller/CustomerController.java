package com.shoppix.customer_service_reactive.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shoppix.customer_service_reactive.bean.Address;
import com.shoppix.customer_service_reactive.bean.Customer;
import com.shoppix.customer_service_reactive.exception.CustomerServiceException;
import com.shoppix.customer_service_reactive.model.*;
import com.shoppix.customer_service_reactive.repo.CustomerRepo;
import com.shoppix.customer_service_reactive.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@CrossOrigin("*")
@RestController
@RequestMapping("/customer")
@Slf4j
public class CustomerController {
    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    public CustomerService customerService;

    @Autowired
    public WebClient.Builder webClientBuilder;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    private static final String CUSTOMER_SERVICE = "customerService";

    private static final int DEFAULT_PRODUCT_QUANTITY = 8;

    public static final String CART_SERVICE_URL = "http://cart-service/cart";

    public static final String CHECKOUT_SERVICE_URL = "http://checkout-service/checkout";

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN
     * <p>
     * API TO REGISTER FIRST TIME NEW CUSTOMER INFORMATION
     *
     * @param customer
     * @return
     * @throws CustomerServiceException
     */
    @PostMapping("/createCustomer")
    @Transactional
    @ResponseBody
    public ResponseEntity<Mono<Customer>> createCustomer(@RequestBody Customer customer) throws CustomerServiceException {

        LOGGER.info("INITIALIZING WITH NEW CUSTOMER REGISTRATION PROCESS");
        Mono<Customer> customerMono = customerService.createOrUpdateCustomer(customer);
        return new ResponseEntity<>(customerMono, HttpStatus.OK);
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN
     * <p>
     * API TO ADD NEW ADDRESS FOR CUSTOMER
     *
     * @param emailId
     * @param address
     * @return
     * @throws CustomerServiceException
     */

    @PostMapping("/addNewAddress/{emailId}")
    @ResponseBody
    public ResponseEntity<Mono<Customer>> addNewAddress(@PathVariable("emailId") String emailId,@RequestBody Address address) throws CustomerServiceException{

        LOGGER.info("ADDING NEW ADDRESS FOR CUSTOMER");

        Mono<Customer> updatedCustomerMono = customerService.addYourNewAddress(emailId,address);

        return new ResponseEntity<>(updatedCustomerMono, HttpStatus.OK);

    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO FETCH CUSTOMER INFORMATION BY CUSTOMER ID
     *
     * @param customerId
     * @return
     * @throws CustomerServiceException
     */
    @GetMapping("/getCustomerById/{customerId}")
    public ResponseEntity<Mono<Customer>> getCustomerById(@PathVariable("customerId") int customerId) throws CustomerServiceException {

        Mono<Customer> customerMono = customerService.getCustomerById(customerId);

        return new ResponseEntity<>(customerMono, HttpStatus.OK);

    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO FETCH CUSTOMER INFORMATION BY EMAIL ADDRESS
     *
     * @param emailId
     * @return
     * @throws CustomerServiceException
     */
    @GetMapping("/getCustomerByEmailId/{emailId}")
    public ResponseEntity<Mono<Customer>> getCustomerByEmailId(@PathVariable("emailId") String emailId) throws CustomerServiceException {

        Mono<Customer> customerInfo = customerService.getCustomerByEmail(emailId);

        return new ResponseEntity<>(customerInfo, HttpStatus.OK);
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO FETCH DEFAULT DELIVERY ADDRESS OF CUSTOMER REGISTERED WITH US
     *
     * @return
     * @throws CustomerServiceException
     */
    @GetMapping("/getDefaultAddress/{emailId}")
    public ResponseEntity<Mono<Address>> getDefaultAddressSelectedByCustomerEmailId(@PathVariable("emailId") String emailId) throws CustomerServiceException {

        LOGGER.info("FETCHING ADDRESS DETAILS BY CUSTOMER EMAIL [ "+emailId+" ]");
        Mono<Customer> customerDeliveryAddress = customerService.getDefaultAddressSelectedByCustomerEmailId(emailId);
        return new ResponseEntity(customerDeliveryAddress, HttpStatus.OK);
    }


	/**
	 * WILL BE CONTROLLED BY ADMIN ONLY
	 * <p>
	 * API TO FETCH LIST OF ALL CUSTOMERS REGISTERED WITH US
	 *
	 * @return
	 * @throws CustomerServiceException
	 */
	@GetMapping("/getAllCustomers")
	public ResponseEntity<Flux<Customer>> getAllCustomers() throws CustomerServiceException{

        LOGGER.info("FETCHING ALL CUSTOMERS INFO....");
		Flux<Customer> allCustomerInfo = customerService.getAllCustomers();

        return new ResponseEntity<>(allCustomerInfo,HttpStatus.OK);
	}


    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO UPDATE CUSTOMER PROFILE INFO
     *
     * @param updatedCustomerInfo
     * @return
     * @throws CustomerServiceException
     */
	@PutMapping("/updateProfileInfo")
	@ResponseBody
	public ResponseEntity<Customer> updateProfileInfo(@RequestBody Customer updatedCustomerInfo) throws CustomerServiceException{

        LOGGER.info("FETCHING DATA FOR EXISTING CUSTOMER");
		Mono<Customer> updatedCustomerDetails = customerService.createOrUpdateCustomer(updatedCustomerInfo);

        return new ResponseEntity(updatedCustomerDetails, HttpStatus.OK);
	}

	/**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO DELETE CUSTOMER ACCOUNT BY CUSTOMER ID AND EMAIL ADDRESS
     *
     * @param customerId
     * @return
     * @throws CustomerServiceException
     */
    @DeleteMapping("/deleteCustomer/{customerId}")
    public Mono<ResponseEntity<ResponseErrorMessage>> deleteCustomerAccount(@PathVariable("customerId") int customerId) {
        LOGGER.info("FETCHING.. INFO TO DELETE PROFILE WITH CUSTOMER ID [" + customerId + "]");

        return customerService.deleteCustomerById(customerId)
                .flatMap(deleted -> {
                    if (deleted) {
                        // Customer deleted successfully
                        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage(HttpStatus.OK.value(), "CUSTOMER REMOVED SUCCESSFULLY");
                        return Mono.just(ResponseEntity.ok(responseErrorMessage));
                    } else {
                        // Customer not found or could not be deleted
                        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage(HttpStatus.NOT_FOUND.value(), "COULD NOT FIND CUSTOMER WITH ID [" + customerId + "]");
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorMessage));
                    }
                })
                .onErrorResume(e -> {
                    // Error handling
                    LOGGER.error("Error deleting customer with ID [{}]", customerId, e);
                    ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR DELETING CUSTOMER WITH ID [" + customerId + "]");
                    return Mono.just(ResponseEntity.internalServerError().body(responseErrorMessage));
                });
    }





    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO UPDATE ORDER LIST BY CUSTOMER
     *
     * @param customerOrder
     * @return
     * @throws CustomerServiceException
     */
	@PutMapping ("/updateOrderList")
	@ResponseBody
	public ResponseEntity<Flux<Order>> updateOrderListOfCustomer(@RequestBody Order customerOrder) throws CustomerServiceException {

        Flux<Order> orderListUpdated = customerService.updateOrderList(customerOrder);

        return new ResponseEntity<>(orderListUpdated,HttpStatus.OK);
    }

    /*CUSTOMER-CART MICROSERVICE*/

	/**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO ADD PRODUCT TO CART
     *
	 * @param productId
	 * @param customerIdForCart
	 * @param cartProduct
	 * @return
	 */
	@PutMapping("/{customerIdForCart}/addProductToCart/{productId}")
	@ResponseBody
	public ResponseEntity<Mono<CartProduct>> addProductToCart(@PathVariable("productId") int productId, @PathVariable("customerIdForCart") int customerIdForCart, @RequestBody CartProduct cartProduct) throws CustomerServiceException {

        Mono<Cart> cart = webClientBuilder.build()
                .get()
                .uri(CART_SERVICE_URL.concat("/viewCart/customer/").concat(String.valueOf(customerIdForCart)))
                .retrieve()
                .bodyToMono(Cart.class)
                .publishOn(Schedulers.parallel());

        Mono<Cart> updatedCartProducts = customerService.addProductToCart(customerIdForCart,cartProduct);

        Mono<Cart> cartObject = cart.map(cartData -> {
            cartData.setCustomerIdForCart(customerIdForCart);
            cartData.getCartProducts().add(cartProduct);
            cartData.setTotalPrice((int)(cartData.getTotalPrice() + cartProduct.getPrice()));
            try {
                customerService.createOrUpdateCartForCustomer(cartData);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return cartData;
        });

        cart.switchIfEmpty(Mono.error(() -> {
            throw new CustomerServiceException("ERROR FETCHING CART AND PRODUCT DETAILS FOR CUSTOMER ID ["+customerIdForCart+"]");
        })).delaySubscription(Duration.ofMillis(3000));

        return new ResponseEntity(cartProduct,HttpStatus.OK);
	}

	/**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO DELETE PRODUCT FROM CART
     *
     * @param productId
     * @param customerIdForCart
     * @return
     */
	@DeleteMapping("/{customerIdForCart}/deleteProductFromCart/{productId}")
	public ResponseEntity<Mono<AtomicReference<String>>> deleteProductFromCart(@PathVariable("productId") int productId, @PathVariable("customerIdForCart") int customerIdForCart) throws CustomerServiceException {

        AtomicReference<String> cartProductDeleted = new AtomicReference<>("SUCCESS");

        Mono<Cart> cart = webClientBuilder.build()
                .get()
                .uri(CART_SERVICE_URL.concat("/viewCart/customer/").concat(String.valueOf(customerIdForCart)))
                .retrieve()
                .bodyToMono(Cart.class)
                .publishOn(Schedulers.parallel());

        cart.map(customerCart -> customerCart.getCartProducts().stream().filter(cartProduct -> {
            if (cartProduct.getProductId() == productId) {
                customerCart.getCartProducts().remove(cartProduct);
                customerCart.setTotalPrice((int) (customerCart.getTotalPrice()-cartProduct.getPrice()));
                customerCart.setCustomerIdForCart(customerCart.getCustomerIdForCart());
                try {
                    customerService.createOrUpdateCartForCustomer(customerCart);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                cartProductDeleted.set("SUCCESS");
            }
            cartProductDeleted.set("SUCCESS");
            return true;
        })).switchIfEmpty(Mono.error(() -> {
            throw new CustomerServiceException("ERROR DELETING PRODUCT ID ["+productId+"] | NO DATA AVAILABLE");
        })).delaySubscription(Duration.ofMillis(3000));

        return new ResponseEntity("PRODUCT ID ["+productId+"] DELETED FOR CUSTOMER ID ["+customerIdForCart+"]",HttpStatus.OK);
	}

    /*CUSTOMER-ORDER MICROSERVICE*/

	/**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO ORDER PRODUCTS WITH CUSTOMER'S CHOICE
     *
     * @param orderRequest
     * @return
     */
	@PutMapping("/orderProduct")
	@ResponseBody
	public ResponseEntity<Mono<OrderRequest>> orderNow(@RequestBody OrderRequest orderRequest) throws CustomerServiceException {

        webClientBuilder.build()
                .post()
                .uri(CHECKOUT_SERVICE_URL.concat("/checkout/proceedForPayment"))
                .bodyValue(orderRequest)
                .retrieve()
                .toEntity(OrderRequest.class)
                .thenReturn(new ResponseEntity<>(orderRequest,HttpStatus.OK)).delaySubscription(Duration.ofMillis(3000)).subscribe();

		return new ResponseEntity(orderRequest,HttpStatus.OK);
	}

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN
     * <p>
     * API TO VIEW ALL ORDERS OF CUSTOMER BY EMAIL ADDRESS
     *
     * @param emailId
     * @return
     * @throws CustomerServiceException
     */
	@GetMapping("/{emailId}/myOrders")
	public ResponseEntity<Flux<Order>> myOrders(@PathVariable("emailId") String emailId) throws CustomerServiceException {

		LOGGER.info("FETCHING...");
		Flux<Order> myOrders = customerService.getAllOrdersForCustomer(emailId);

		return new ResponseEntity<>(myOrders,HttpStatus.OK);
	}

}