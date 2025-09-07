package com.shoppix.customer_reactive_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.customer_reactive_service.entity.Address;
import com.shoppix.customer_reactive_service.entity.Customer;
import com.shoppix.customer_reactive_service.enums.CartProductEnum;
import com.shoppix.customer_reactive_service.enums.CustomerEnum;
import com.shoppix.customer_reactive_service.events.CartEvent;
import com.shoppix.customer_reactive_service.events.CartProductEvent;
import com.shoppix.customer_reactive_service.events.CustomerEvent;
import com.shoppix.customer_reactive_service.exception.CustomerServiceException;
import com.shoppix.customer_reactive_service.model.Cart;
import com.shoppix.customer_reactive_service.model.CartProduct;
import com.shoppix.customer_reactive_service.model.Order;
import com.shoppix.customer_reactive_service.model.ResponseMessage;
import com.shoppix.customer_reactive_service.repo.CustomerRepo;
import com.shoppix.customer_reactive_service.util.CustomerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    public CustomerRepo customerRepo;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    public CustomerUtil customerUtil;

    @Autowired
    public WebClient.Builder webClientBuilder;

    @Autowired
    public CustomerKafkaProducerService customerKafkaProducerService;

    @Value("${spring.kafka.topic.cart-topic}")
    private String cartTopicName;

    @Value("${spring.kafka.topic.customer-notification-topic}")
    private String notificationTopic;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

    private final ObjectMapper objectMapper;

    public static final String CART_SERVICE_URL = "http://cart-reactive-service/api/v1/cart";

    @Autowired
    public CustomerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * METHOD TO REGISTER FIRST TIME NEW CUSTOMER INFORMATION
     *
     * @param customer
     * @return
     * @throws CustomerServiceException
     */
    public Mono<ResponseMessage> createOrUpdateCustomer(Customer customer) {
        LOGGER.info("FETCHING CUSTOMER EXISTENCE...");

        return customerRepo.findByEmailId(customer.getEmailId())
                // If customer not found, create new one, else update the existing one
                .defaultIfEmpty(null)  // Mono.just() to null if no customer is found
                .flatMap(existingCustomer -> {
                    if (existingCustomer == null) {
                        return createNewCustomer(customer);
                    } else {
                        return updateExistingCustomer(existingCustomer, customer);
                    }
                });
    }

    private Mono<ResponseMessage> createNewCustomer(Customer customer) {
        LOGGER.info("CUSTOMER REGISTRATION IN PROGRESS");

        Customer newCustomer = new Customer();
        newCustomer.setCustomerId(customerUtil.generateCustomerId());
        newCustomer.setCustomerName(customer.getCustomerName());
        newCustomer.setPhone(customer.getPhone());
        newCustomer.setPassword(encoder.encode(customer.getPassword()));
        newCustomer.setEmailId(customer.getEmailId());
        newCustomer.setAddress(customer.getAddress());
        newCustomer.setMyOrders(new ArrayList<>());
        newCustomer.setEventStatus(CustomerEnum.CUSTOMER_REGISTERED.name());
        newCustomer.setCreatedDateTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
        newCustomer.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
        newCustomer.setAccountExistence(true);

        Cart cart = new Cart();
        cart.setCustomerIdForCart(newCustomer.getCustomerId());
        cart.setCartProducts(new ArrayList<>());

        return customerRepo.insert(newCustomer)
                .subscribeOn(Schedulers.parallel())  // Ensure to run on parallel thread
                .doOnSuccess(savedCustomer -> {
                    LOGGER.info("CUSTOMER AND THEIR CART CREATED SUCCESSFULLY");
                    try {
                        cart.setEventStatus(CustomerEnum.CUSTOMER_REGISTERED.name());
                        cart.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
                        createOrUpdateCartForCustomer(cart);

                        // Send notification and log process completion
                        sendNotificationToCustomer(CustomerEnum.CUSTOMER_REGISTERED.name(), newCustomer.getCustomerId())
                                .doOnTerminate(() -> LOGGER.info("Process completed"))
                                .subscribe();

                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(savedCustomer -> ResponseMessage.builder()
                        .statusCode(200)
                        .message("CUSTOMER REGISTERED")
                        .timestamp(LocalDateTime.now().toString())
                        .responseData(savedCustomer)
                        .errorDetails(null)
                        .path("/customer/createOrUpdateCustomer")
                        .build())
                .onErrorResume(e -> {
                    LOGGER.error("OOPS TECHNICAL ERROR! NEW CUSTOMER REGISTRATION FAILED", e);
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(500)
                            .message("OOPS TECHNICAL ERROR! NEW CUSTOMER REGISTRATION FAILED")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(null)
                            .errorDetails(List.of(e.getMessage()))
                            .path("/customer/createOrUpdateCustomer")
                            .build());
                });
    }

    private Mono<ResponseMessage> updateExistingCustomer(Customer existingCustomer, Customer updatedCustomer) {
        LOGGER.info("UPDATING EXISTING CUSTOMER...");

        existingCustomer.setCustomerName(updatedCustomer.getCustomerName());
        existingCustomer.setEmailId(updatedCustomer.getEmailId());
        existingCustomer.setAddress(updatedCustomer.getAddress());
        existingCustomer.setPassword(updatedCustomer.getPassword());
        existingCustomer.setPhone(updatedCustomer.getPhone());
        existingCustomer.getMyOrders().addAll(updatedCustomer.getMyOrders());
        existingCustomer.setEventStatus(CustomerEnum.CUSTOMER_REGISTERED.name());
        existingCustomer.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
        existingCustomer.setAccountExistence(true);

        // Save the updated customer
        return customerRepo.save(existingCustomer)
                .map(savedCustomer -> ResponseMessage.builder()
                        .statusCode(200)
                        .message("CUSTOMER UPDATED SUCCESSFULLY")
                        .timestamp(LocalDateTime.now().toString())
                        .responseData(savedCustomer)
                        .errorDetails(null)
                        .path("/customer/createOrUpdateCustomer")
                        .build())
                .onErrorResume(e -> {
                    LOGGER.error("Failed to update customer", e);
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(500)
                            .message("FAILED TO UPDATE CUSTOMER")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(null)
                            .errorDetails(List.of(e.getMessage()))
                            .path("/customer/createOrUpdateCustomer")
                            .build());
                });
    }

    public Mono<ResponseMessage> addYourNewAddress(String emailId, Address address) throws CustomerServiceException{

        LOGGER.info("UPDATING NEW ADDRESS DETAILS FOR CUSTOMER [" +emailId+"]");
        Mono<Customer> existingCustomerData = customerRepo.findByEmailId(emailId);
        return existingCustomerData
                .publishOn(Schedulers.parallel())  // Use parallel scheduler to ensure non-blocking operations
                .flatMap(existingCustomer -> {
                    existingCustomer.getAddress().add(address);
                    return customerRepo.save(existingCustomer)
                            .map(updatedCustomer -> ResponseMessage.builder()
                                    .statusCode(200)
                                    .message("NEW ADDRESS ADDED SUCCESSFULLY")
                                    .timestamp(LocalDateTime.now().toString())
                                    .responseData(updatedCustomer)
                                    .errorDetails(null)
                                    .path("/customer/addNewAddress/"+emailId)
                                    .build())
                            .onErrorResume(e -> {
                                LOGGER.error("Error saving updated customer address", e);
                                // Return an error response message in case of failure
                                return Mono.just(ResponseMessage.builder()
                                        .statusCode(500)
                                        .message("ERROR SAVING UPDATED CUSTOMER ADDRESS")
                                        .timestamp(LocalDateTime.now().toString())
                                        .responseData(null)
                                        .errorDetails(List.of(e.getMessage()))
                                        .path("/customer/addNewAddress/"+emailId)
                                        .build());
                            });
                });
    }

    /**
     * WILL BE CONTROLLED BY ADMIN
     * <p>
     * METHOD FOR FETCHING CUSTOMER INFORMATION BY CUSTOMER ID
     *
     * @param customerId
     * @return
     * @throws CustomerServiceException
     */
    public Mono<ResponseMessage> getCustomerById(int customerId) {
        LOGGER.info("FETCHING CUSTOMER DETAILS WITH CUSTOMER ID [" + customerId + "]...");

        // Fetch customer by ID from the repository
        Mono<Customer> customerMono = customerRepo.findById(customerId);

        return customerMono
                .publishOn(Schedulers.parallel()) // Ensure parallel execution
                .flatMap(customer -> {
                    if (customer == null) {
                        // If no customer is found, return a not-found response
                        LOGGER.error("Customer with ID " + customerId + " not found");
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(404)
                                .message("CUSTOMER NOT FOUND")
                                .timestamp(LocalDateTime.now().toString())
                                .responseData(null)
                                .errorDetails(List.of("CUSTOMER WITH ID " + customerId + " DOES NOT EXIST"))
                                .path("/customer/getCustomerById/"+customerId)
                                .build());
                    } else {
                        // If customer is found, return the customer details in the response
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(200)
                                .message("CUSTOMER DETAILS FETCHED SUCCESSFULLY")
                                .timestamp(LocalDateTime.now().toString())
                                .responseData(customer)
                                .errorDetails(null)
                                .path("/customer/getCustomerById/"+customerId)
                                .build());
                    }
                })
                .onErrorResume(e -> {
                    // Handle any unexpected error and return a generic error response
                    LOGGER.error("ERROR FETCHING CUSTOMER DETAILS", e);
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(500)
                            .message("ERROR FETCHING CUSTOMER DETAILS")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(null)
                            .errorDetails(List.of(e.getMessage()))
                            .path("/customer/getCustomerById/"+customerId)
                            .build());
                });
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * METHOD TO FETCH CUSTOMER INFORMATION BY EMAIL ADDRESS
     *
     * @param emailId
     * @return
     * @throws CustomerServiceException
     */
    public Mono<ResponseMessage> getCustomerByEmail(String emailId) throws CustomerServiceException {

        Mono<Customer> customerMono = customerRepo.findByEmailId(emailId);

        return customerMono
                .publishOn(Schedulers.parallel()) // Ensure parallel execution
                .flatMap(customer -> {
                    if (customer == null) {
                        // If no customer is found, return a not-found response
                        LOGGER.error("CUSTOMER WITH EMAIL ID [" + emailId + "] NOT FOUND");
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(404)
                                .message("CUSTOMER NOT FOUND")
                                .timestamp(LocalDateTime.now().toString())
                                .responseData(null)
                                .errorDetails(List.of("CUSTOMER WITH EMAIL ID " + emailId + " DOES NOT EXIST"))
                                .path("/customer/getCustomerByEmail/"+emailId)
                                .build());
                    } else {
                        // If customer is found, return the customer details in the response
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(200)
                                .message("CUSTOMER DETAILS FETCHED SUCCESSFULLY")
                                .timestamp(LocalDateTime.now().toString())
                                .responseData(customer)
                                .errorDetails(null)
                                .path("/customer/getCustomerByEmail/"+emailId)
                                .build());
                    }
                })
                .onErrorResume(e -> {
                    // Handle any unexpected error and return a generic error response
                    LOGGER.error("ERROR FETCHING CUSTOMER DETAILS WITH EMAIL ID [" + emailId + "]", e);
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(500)
                            .message("ERROR FETCHING CUSTOMER DETAILS ")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(null)
                            .errorDetails(List.of(e.getMessage()))
                            .path("/customer/getCustomerByEmail/"+emailId)
                            .build());
                });
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * METHOD TO FETCH CUSTOMER ADDRESS BY EMAIL ID
     *
     * @param emailId
     * @return
     * @throws CustomerServiceException
     */
    public Mono<ResponseMessage> getDefaultAddressSelectedByCustomerEmailId(String emailId) {
        LOGGER.info("SERVICE ADDRESS - FETCHING CUSTOMER ADDRESS");

        // Fetch customer details by email
        Mono<Customer> customerDetails = customerRepo.findByEmailId(emailId);

        // Process customer details to filter for default address
        Mono<Customer> customerFinalInfo = customerDetails.publishOn(Schedulers.parallel()).map(customer -> {
            // Filter the default address
            customer.setAddress(customer.getAddress().stream()
                    .filter(Address::isDefaultAddress)
                    .collect(Collectors.toList()));
            return customer;
        });

        return customerFinalInfo
                .flatMap(customer -> {
                    // If no default address found, return an error response
                    if (customer.getAddress().isEmpty()) {
                        LOGGER.error("NO DEFAULT ADDRESS FOUND FOR CUSTOMER WITH EMAIL ID [" + emailId + "]");
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(404)
                                .message("NO DEFAULT ADDRESS FOUND")
                                .timestamp(LocalDateTime.now().toString())
                                .responseData(null)
                                .errorDetails(List.of("NO DEFAULT ADDRESS FOUND FOR CUSTOMER WITH EMAIL ID " + emailId))
                                .path("/customer/getDefaultAddressSelectedByCustomerEmailId/"+emailId)
                                .build());
                    } else {
                        // If customer with default address is found, return success response
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(200)
                                .message("DEFAULT ADDRESS FETCHED SUCCESSFULLY")
                                .timestamp(LocalDateTime.now().toString())
                                .responseData(customer.getAddress()) // Send the default address(es)
                                .errorDetails(null)
                                .path("/customer/getDefaultAddressSelectedByCustomerEmailId/"+emailId)
                                .build());
                    }
                })
                .onErrorResume(e -> {
                    // Handle errors such as customer retrieval issues
                    LOGGER.error("Error fetching customer details for email ID [" + emailId + "]", e);
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(500)
                            .message("Error fetching customer address")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(null)
                            .errorDetails(List.of(e.getMessage()))
                            .path("/customer/getDefaultAddressSelectedByCustomerEmailId")
                            .build());
                });
    }

    /**
     * WILL BE CONTROLLED BY ADMIN
     * <p>
     * METHOD TO FETCH LIST OF CUSTOMERS REGISTERED WITH US
     *
     * @return
     * @throws CustomerServiceException
     */
    public Mono<ResponseMessage> getAllCustomers() throws CustomerServiceException {

        Flux<Customer> allCustomerInfo = customerRepo.findAll();

        // Process customer data
        return allCustomerInfo.collectList() // Collect customers into a list
                .publishOn(Schedulers.parallel()) // Ensure parallel execution
                .flatMap(customers -> {
                    if (customers.isEmpty()) {
                        // If no customers are found, return an error response
                        LOGGER.error("No customers found");
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(404)
                                .message("No customers found")
                                .timestamp(LocalDateTime.now().toString())
                                .responseData(null)
                                .errorDetails(List.of("No customer data available"))
                                .path("/customer/getAllCustomers")
                                .build());
                    } else {
                        // If customers are found, return a success response with the customers
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(200)
                                .message("All customers fetched successfully")
                                .timestamp(LocalDateTime.now().toString())
                                .responseData(customers) // Send customer data
                                .errorDetails(null)
                                .path("/customer/getAllCustomers")
                                .build());
                    }
                })
                .onErrorResume(e -> {
                    // Handle any unexpected error during the fetching process
                    LOGGER.error("Error fetching all customer info", e);
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(500)
                            .message("Error fetching all customer info")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(null)
                            .errorDetails(List.of(e.getMessage()))
                            .path("/customer/getAllCustomers")
                            .build());
                });
    }

    /**
     * WILL BE CONTROLLED BY ADMIN ONLY
     * <p>
     * METHOD TO DELETE CUSTOMER ACCOUNT BY CUSTOMER ID UPON INACTIVITY FOR LONG TIME
     *
     * @param customerId
     * @throws CustomerServiceException
     */
    public Mono<ResponseMessage> deleteCustomerById(int customerId) {
        LOGGER.info("IN PROCESS OF DELETING PROFILE WITH CUSTOMER ID [" + customerId + "]");

        return sendNotificationToCustomer(CustomerEnum.CUSTOMER_DELETED.name(), customerId)
                .then(customerRepo.deleteById(customerId)) // Delete customer by ID
                .then(deleteCartWhenCustomerIsDeleted(customerId)) // Delete customer cart (if needed)
                .then(Mono.just(true)) // Return true indicating success
                .flatMap(deletionSuccess -> {
                    // Return success response
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(200)
                            .message("Customer profile and associated cart deleted successfully.")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(true) // No data to return
                            .errorDetails(null)
                            .path("/customer/deleteCustomerById")
                            .build());
                })
                .onErrorResume(e -> {
                    // Handle error and return error response
                    LOGGER.error("Error during customer deletion process", e);
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(500)
                            .message("Error during customer deletion process")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(false)
                            .errorDetails(List.of(e.getMessage()))
                            .path("/customer/deleteCustomerById")
                            .build());
                });
    }


    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * METHOD TO UPDATE ORDER LIST FOR CUSTOMER
     *
     * @param customerOrder
     * @return
     */
    public Mono<ResponseMessage> updateOrderList(Order customerOrder) throws CustomerServiceException {

        Mono<Customer> customerData = customerRepo.findByEmailId(customerOrder.getCustomerEmailId());

        return customerData
                .publishOn(Schedulers.parallel()) // Ensuring it's executed on a parallel thread
                .flatMap(customer -> {
                    if (customer != null) {
                        // Add the order to the customer's order list
                        customer.getMyOrders().add(customerOrder);
                        return createOrUpdateCustomer(customer) // Reactive way of creating/updating customer
                                .thenReturn(customer); // Ensure we return the customer after saving
                    } else {
                        return Mono.error(new CustomerServiceException("Customer not found with email ID [" + customerOrder.getCustomerEmailId() + "]"));
                    }
                })
                .flatMap(updatedCustomer -> {
                    // Return the updated list of orders as the response
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(200)
                            .message("Order list updated successfully.")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(updatedCustomer.getMyOrders()) // Return updated order list
                            .errorDetails(null)
                            .path("/order/updateOrderList")
                            .build());
                })
                .onErrorResume(e -> {
                    // Handle errors and construct an error response
                    LOGGER.error("Error updating order list for customer with email ID [" + customerOrder.getCustomerEmailId() + "]", e);
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(500)
                            .message("Error updating order list.")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(null)
                            .errorDetails(List.of(e.getMessage()))
                            .path("/order/updateOrderList")
                            .build());
                });
    }

    /*CUSTOMER-ORDER MICROSERVICE*/

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     *
     * METHOD TO GET ALL ORDERS OF REGISTERED CUSTOMER
     *
     * @param emailId
     * @throws CustomerServiceException
     */
    public Mono<ResponseMessage> getAllOrdersForCustomer(String emailId) {

        LOGGER.info("FETCHING... YOUR RECENT ORDERS");

        // Retrieve customer details by email
        Mono<Customer> completeCustomerInfo = customerRepo.findByEmailId(emailId);

        return completeCustomerInfo
                .publishOn(Schedulers.parallel()) // Ensure the processing happens on a parallel thread
                .flatMap(customer -> {
                    // Get all orders for the customer
                    Flux<Order> customerOrders = Flux.fromIterable(customer.getMyOrders());

                    // If no orders exist, return an error response
                    return customerOrders
                            .collectList() // Collect the orders into a list
                            .flatMap(orders -> {
                                if (orders.isEmpty()) {
                                    // If no orders are found, return a structured error message
                                    return Mono.just(ResponseMessage.builder()
                                            .statusCode(404)
                                            .message("YOU HAVEN'T ORDERED ANYTHING YET")
                                            .timestamp(LocalDateTime.now().toString())
                                            .responseData(null)
                                            .errorDetails(null)
                                            .path("/order/getAllOrdersForCustomer")
                                            .build());
                                }
                                // If orders are found, return them as part of the success response
                                return Mono.just(ResponseMessage.builder()
                                        .statusCode(200)
                                        .message("Successfully fetched all orders.")
                                        .timestamp(LocalDateTime.now().toString())
                                        .responseData(orders)
                                        .errorDetails(null)
                                        .path("/order/getAllOrdersForCustomer")
                                        .build());
                            });
                })
                .onErrorResume(e -> {
                    // If any error occurs during the process, handle it and return an error response
                    LOGGER.error("Error fetching orders for customer with email ID [" + emailId + "]", e);
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(500)
                            .message("Error fetching orders.")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(null)
                            .errorDetails(List.of(e.getMessage()))
                            .path("/order/getAllOrdersForCustomer")
                            .build());
                });
    }


    /*CUSTOMER-CART MICROSERVICE*/

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * METHOD TO ADD PRODUCT TO CART
     *
     * @param customerIdForCart
     * @param cartProduct
     * @return
     */
    public Mono<ResponseMessage> addProductToCart(int customerIdForCart,String productId,CartProduct cartProduct){

        LOGGER.info("ADDING PRODUCT TO CART...");

        try{
            CartProductEvent cartProductEvent = new CartProductEvent();
            cartProductEvent.setCustomerIdForCart(customerIdForCart);
            cartProductEvent.setProductId(productId);
            cartProductEvent.setCartProductMessageType(CartProductEnum.ADD_CART_PRODUCT.name());
            cartProductEvent.setCartProduct(cartProduct);

            String cartAsMessage = objectMapper.writeValueAsString(cartProductEvent);

            customerKafkaProducerService.sendMessage(cartTopicName, cartAsMessage);
//                    .doOnSuccess(prod -> LOGGER.info("PRODUCTS ADDED TO CART"))
//                    .doOnError(e -> LOGGER.error("ERROR DURING ADDING PRODUCTS TO CART", e))// Subscribe to trigger the asynchronous call

            // Return a success response wrapped in Mono
            return Mono.just(ResponseMessage.builder()
                    .statusCode(200)
                    .message("Product added to cart successfully.")
                    .timestamp(LocalDateTime.now().toString())
                    .responseData(cartProduct)
                    .errorDetails(null)
                    .path("/customer/"+customerIdForCart+"/addProductToCart")
                    .build());

        } catch (Exception e) {
            LOGGER.error("ERROR DURING ADDING PRODUCTS TO CART", e);

            // Return an error response wrapped in Mono in case of any failure
            return Mono.just(ResponseMessage.builder()
                    .statusCode(500)
                    .message("Failed to add product to cart.")
                    .timestamp(LocalDateTime.now().toString())
                    .responseData(null)
                    .errorDetails(List.of(e.getMessage()))
                    .path("/customer/"+customerIdForCart+"/addProductToCart")
                    .build());
        }

//        Mono<Cart> existingCustomerCart = webClientBuilder.build()
//                .get()
//                .uri(CART_SERVICE_URL.concat("/viewCart/customer/").concat(String.valueOf(customerIdForCart)))
//                .retrieve()
//                .bodyToMono(Cart.class)
//                .publishOn(Schedulers.parallel());

//        Mono<Product> product = webClientBuilder.build()
//                .get()
//                .uri(PRODUCT_SERVICE_URL.concat("/filterProductById/"+cartProduct.getProductId()))
//                .retrieve()
//                .bodyToMono(Product.class)
//                .subscribeOn(Schedulers.parallel());

//        Flux<CartProduct> cartProducts = existingCustomerCart.flatMapIterable(Cart::getCartProducts);
//
//        return existingCustomerCart.publishOn(Schedulers.parallel()).map(existingCart -> {
//            existingCart.setCustomerIdForCart(customerIdForCart);
//            product.map(prod-> {
//                CartProduct cartProductToAdd = new CartProduct();
//                cartProductToAdd.setProductId(prod.getProductId());
//                cartProductToAdd.setProductName(prod.getProductName());
//
//                cartProductToAdd.setPrice(prod.getPrice());
//                cartProductToAdd.setStockStatus(prod.getStockStatus());
//                cartProductToAdd.setQuantity(cartProduct.getQuantity());
//
//                Mono<CartProduct> filteredCartProduct = cartProducts.filter(cp -> cp.getProductId() == cartProduct.getProductId()).single();
//                Mono<CartProduct> filteredCartProductsMono = filteredCartProduct != null ? filteredCartProduct.switchIfEmpty(Mono.defer((Supplier<? extends Mono<? extends CartProduct>>) () -> {
//                    LOGGER.info("FINAL CART PRODUCT" + cartProduct);
//                    existingCart.getCartProducts().add(cartProduct);
//                    return Mono.just(cartProduct);
//                })) : filteredCartProduct.map(fcp -> {
//                    LOGGER.info("UPDATING EXISTING CART PRODUCT " + filteredCartProduct);
//                    CartProduct updatedCartProduct = new CartProduct();
//                    updatedCartProduct.setProductId(cartProduct.getProductId());
//                    updatedCartProduct.setProductName(cartProduct.getProductName());
//                    updatedCartProduct.setPrice(fcp.getPrice() + cartProduct.getPrice());
//                    updatedCartProduct.setQuantity(fcp.getQuantity() + cartProduct.getQuantity());
//                    LOGGER.info("FILTERED CART PRODUCT" + filteredCartProduct);
//                    existingCart.getCartProducts().add(updatedCartProduct);
//                    return updatedCartProduct;
//                });
//                existingCart.setTotalPrice((int) (existingCart.getTotalPrice() + (cartProduct.getPrice() * cartProduct.getQuantity())));
//                try {
//                    createOrUpdateCartForCustomer(existingCart);
//                } catch (JsonProcessingException e) {
//                    throw new RuntimeException(e);
//                }
//                return cartProductToAdd;
//            });
//            return existingCart;
//        });
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * METHOD TO DELETE PRODUCT FROM CART
     *
     * @param customerIdForCart
     * @param productId
     * @return
     * @throws CustomerServiceException
     */
    public Mono<ResponseMessage> deleteProductsFromCart(int customerIdForCart, int productId, CartProduct cartProduct) throws CustomerServiceException{

        LOGGER.info("REMOVING PRODUCT FROM CART...");



        //        Mono<Cart> customerCart = webClientBuilder.build()
//                .get()
//                .uri(CART_SERVICE_URL.concat("/viewCart/customer/").concat(String.valueOf(customerIdForCart)))
//                .retrieve()
//                .bodyToMono(Cart.class)
//                .publishOn(Schedulers.parallel());
//
//        AtomicReference<String> cartProductDeleted = new AtomicReference<>("SUCCESS");
//
//        Mono<AtomicReference<String>> productInCartMessage = customerCart.map(custoCart -> {
//            custoCart.getCartProducts().stream().forEach(cartProduct -> {
//                if(cartProduct.getProductId() == productId){
//                    custoCart.getCartProducts().remove(cartProduct);
//                    custoCart.setTotalPrice((int) (custoCart.getTotalPrice()-cartProduct.getPrice()));
//                    custoCart.setCustomerIdForCart(custoCart.getCustomerIdForCart());
//                    try {
//                        createOrUpdateCartForCustomer(custoCart);
//                    } catch (JsonProcessingException e) {
//                        throw new RuntimeException(e);
//                    }
//                    cartProductDeleted.set("SUCCESS");
//                }
//            });
//            return cartProductDeleted;
//        });
//        return productInCartMessage;
        return null;
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * METHOD TO CREATE CART FOR CUSTOMER VIA KAFKA MESSAGING QUEUE
     *
     * @param cart
     */
    public void createOrUpdateCartForCustomer(Cart cart) throws JsonProcessingException {

        LOGGER.debug("CART INITIATION FOR CUSTOMER ID ["+ cart.getCustomerIdForCart()+"] IN PROGRESS");

        try{
            CartEvent cartEvent = new CartEvent();
            cartEvent.setCustomerIdForCart(cart.getCustomerIdForCart());
            cartEvent.setCartMessageType(CustomerEnum.CUSTOMER_REGISTERED.name());
            cartEvent.setCartMessage(cart);

            String cartAsMessage = objectMapper.writeValueAsString(cartEvent);

            customerKafkaProducerService.sendMessage(cartTopicName, cartAsMessage);
            LOGGER.info("CUSTOMER REGISTERED, AN EMPTY CART CREATED SUCCESSFULLY");
        } catch(Exception e){
            deleteCustomerById(cart.getCustomerIdForCart()).subscribe();
            LOGGER.error("TECHNICAL ISSUES ON DURING CART INITIATION!");
        }
    }

    private String generateLastUpdatedDateTime(Date date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        return simpleDateFormat.format(date);
    }

    public Mono<Boolean> deleteCartWhenCustomerIsDeleted(int customerId) {
        LOGGER.info("IN PROCESS OF DELETING CART FOR CUSTOMER ID [" + customerId + "]");

        try{
            CartEvent cartEvent = new CartEvent();
            cartEvent.setCustomerIdForCart(customerId);
            cartEvent.setCartMessageType(CustomerEnum.CUSTOMER_DELETED.name());
            cartEvent.setCartMessage(new Cart());

            String cartAsMessage = objectMapper.writeValueAsString(cartEvent);
            customerKafkaProducerService.sendMessage(cartTopicName,cartAsMessage);
            LOGGER.info("REQUEST FOR CART TO BE DELETED AS CUSTOMER IS DEACTIVATING");
            return Mono.just(true);
        } catch(Exception e){
            customerRepo.deleteById(customerId).subscribe();
            LOGGER.error("TECHNICAL ISSUES ON DURING CART INITIATION!");
        }
        return Mono.just(false);
    }

    public Mono<Void> sendNotificationToCustomer(String eventType, int customerId) {
        LOGGER.info("EVENT AND CUSTOMER ID {}, {}", eventType, customerId);

        return customerRepo.findById(customerId)
                .flatMap(customer -> {
                    CustomerEvent customerEvent = new CustomerEvent();
                    customerEvent.setCustomerId(customer.getCustomerId());
                    customerEvent.setCustomerEmail(customer.getEmailId());
                    customerEvent.setCustomerName(customer.getCustomerName());
                    customerEvent.setPhone(customer.getPhone());
                    customerEvent.setCustomerMessageType(eventType);

                    LOGGER.info("CUSTOMER EVENT: {}", customerEvent);

                    String customerAsMessage;
                    try {
                        LOGGER.info("SENDING OUT NOTIFICATION TO THE CUSTOMER");
                        customerAsMessage = objectMapper.writeValueAsString(customerEvent);
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Error serializing customer event: ", e);
                        return Mono.error(new RuntimeException("Error serializing customer event", e));
                    }

                    // Send the message to Kafka and return Mono<Void> to continue the chain
                    return Mono.fromRunnable(() -> {
                        customerKafkaProducerService.sendMessage(notificationTopic, customerAsMessage);
                    }).then();  // Returning Mono<Void> here to indicate completion
                })
                .switchIfEmpty(Mono.defer(() -> {
                    LOGGER.warn("Customer with ID {} not found", customerId);
                    return Mono.empty(); // Return Mono.empty() if customer not found
                }));
    }


}