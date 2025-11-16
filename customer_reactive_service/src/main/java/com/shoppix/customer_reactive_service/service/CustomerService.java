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
import com.shoppix.customer_reactive_service.model.*;
import com.shoppix.customer_reactive_service.repo.CustomerRepo;
import com.shoppix.customer_reactive_service.util.CustomerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
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
                .flatMap(existingCustomer -> updateExistingCustomer(existingCustomer, customer))
                .switchIfEmpty(createNewCustomer(customer));
    }


    private Mono<ResponseMessage> createNewCustomer(Customer customerRequest) {
        LOGGER.info("CUSTOMER REGISTRATION IN PROGRESS");

        Customer newCustomer = prepareCustomer(customerRequest);

        Cart cart = new Cart();
        cart.setCustomerIdForCart(newCustomer.getCustomerId());
        cart.setCartProducts(new ArrayList<>());

        return customerRepo.insert(newCustomer)
                .subscribeOn(Schedulers.parallel())
                .flatMap(savedCustomer -> buildResponse(200,"CUSTOMER REGISTERED ["+newCustomer.getEmailId()+"]",LocalDateTime.now().toString(),savedCustomer,new ArrayList<>(),"/customer/createOrUpdateCustomer"))
                .doOnSuccess(savedCustomer -> {
                    try {
                        cart.setEventStatus(CustomerEnum.CUSTOMER_REGISTERED.name());
                        cart.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
                        createOrUpdateCartForCustomer(cart);

                        sendNotificationToCustomer(CustomerEnum.CUSTOMER_REGISTERED.name(), newCustomer.getCustomerId())
                                .doOnTerminate(() -> LOGGER.info("Sending Notification Process completed"))
                                .subscribe();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .log("CUSTOMER AND THEIR CART CREATED SUCCESSFULLY")
                .doOnError(e -> LOGGER.error("OOPS TECHNICAL ERROR! NEW CUSTOMER REGISTRATION FAILED", e))
                .onErrorResume(e -> buildResponse(500,"OOPS TECHNICAL ERROR! NEW CUSTOMER REGISTRATION FAILED",LocalDateTime.now().toString(),null,List.of(e.getMessage()),"/customer/createOrUpdateCustomer"));
    }

    private Customer prepareCustomer(Customer customerRequest) {

        Customer newCustomer = new Customer();
        newCustomer.setCustomerId(customerUtil.generateCustomerId());
        newCustomer.setCustomerName(customerRequest.getCustomerName());
        newCustomer.setPhone(customerRequest.getPhone());
        newCustomer.setPassword(encoder.encode(customerRequest.getPassword()));
        newCustomer.setEmailId(customerRequest.getEmailId());
        newCustomer.setAddress(customerRequest.getAddress());
        newCustomer.setMyOrders(new ArrayList<>());
        newCustomer.setEventStatus(CustomerEnum.CUSTOMER_REGISTERED.name());
        newCustomer.setCreatedDateTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.ssss z").format(new Date()));
        newCustomer.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
        newCustomer.setAccountExistence(true);

        return newCustomer;
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

        return customerRepo.save(existingCustomer)
                .log("UPDATING... CUSTOMER DATA IN PROGRESS")
                .flatMap(savedCustomer -> buildResponse(200,"CUSTOMER UPDATED SUCCESSFULLY",LocalDateTime.now().toString(),savedCustomer,new ArrayList<>(),"/customer/createOrUpdateCustomer"))
                .doOnSuccess(responseMessage -> LOGGER.info("CUSTOMER UPDATED SUCCESSFULLY"))
                .doOnError(e -> LOGGER.error("FAILED TO UPDATE CUSTOMER WITH ID ["+existingCustomer.getCustomerId()+"]",e))
                .onErrorResume(e-> buildResponse(500,"FAILED TO UPDATE CUSTOMER WITH ID ["+existingCustomer.getCustomerId()+"]",LocalDateTime.now().toString(),null,List.of(e.getMessage()),"/customer/createOrUpdateCustomer"));
    }

    public Mono<ResponseMessage> addYourNewAddress(String emailId, Address address) throws CustomerServiceException{

        Mono<Customer> existingCustomerData = customerRepo.findByEmailId(emailId);
        return existingCustomerData
                .publishOn(Schedulers.parallel())
                .flatMap(existingCustomer -> {
                    existingCustomer.getAddress().add(address);
                    return customerRepo.save(existingCustomer)
                            .log("UPDATING NEW ADDRESS DETAILS FOR CUSTOMER [" +emailId+"]")
                            .flatMap(updatedCustomer -> buildResponse(200,"NEW ADDRESS ADDED SUCCESSFULLY", LocalDateTime.now().toString(),updatedCustomer,new ArrayList<>(),"/customer/addNewAddress/"+emailId))
                            .doOnSuccess(responseMessage -> LOGGER.info("NEW ADDRESS ADDED SUCCESSFULLY"))
                            .doOnError(e -> LOGGER.error("ERROR SAVING UPDATED CUSTOMER ADDRESS FOR CUSTOMER ["+emailId+"]",e))
                            .onErrorResume(e -> buildResponse(500,"ERROR SAVING UPDATED CUSTOMER ADDRESS FOR CUSTOMER ["+emailId+"]",LocalDateTime.now().toString(),null,List.of(e.getMessage()),"/customer/addNewAddress/"+emailId));
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

        return customerRepo.findById(customerId)
                .publishOn(Schedulers.parallel())
                .log("FETCHING CUSTOMER DETAILS WITH CUSTOMER ID [" + customerId + "]...")
                .flatMap(customer -> buildResponse(200,"CUSTOMER DETAILS FETCHED SUCCESSFULLY", LocalDateTime.now().toString(),customer,new ArrayList<>(),"/customer/getCustomerById/" + customerId))
                .doOnSuccess(responseMessage ->  LOGGER.info("CUSTOMER DETAILS FETCHED SUCCESSFULLY"))
                .switchIfEmpty(Mono.defer(() -> buildResponse(404,"CUSTOMER WITH ID " + customerId + " DOES NOT EXIST", LocalDateTime.now().toString(),null,new ArrayList<>(),"/customer/getCustomerById/" + customerId)))
                .doOnError(e -> LOGGER.error("ERROR FETCHING CUSTOMER DETAILS WITH CUSTOMER ID [" + customerId + "]"))
                .onErrorResume(e -> buildResponse(500,"ERROR FETCHING CUSTOMER DETAILS WITH CUSTOMER ID [" + customerId + "]", LocalDateTime.now().toString(),null,List.of(e.getMessage()),"/customer/getCustomerById/" + customerId))
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
                .publishOn(Schedulers.parallel())
                .log("FETCHING CUSTOMERS WITH EMAIL ID [" + emailId + "]...")
                .flatMap(customer -> buildResponse(200,"CUSTOMER WITH EMAIL ID [" + emailId + "] FOUND",LocalDateTime.now().toString(),customer,new ArrayList<>(),"/customer/getCustomerByEmail/" + emailId)
                .doOnSuccess(responseMessage ->  LOGGER.info("CUSTOMER DETAILS FETCHED SUCCESSFULLY"))
                .switchIfEmpty(Mono.defer(() -> buildResponse(404,"OOPS ! CUSTOMER WITH EMAIL ID [" + emailId + "] DOES NOT EXIST",LocalDateTime.now().toString(),null,new ArrayList<>(),"/customer/getCustomerByEmail/" + emailId)))
                .doOnError(e -> LOGGER.error("ERROR FETCHING CUSTOMER DETAILS WITH EMAIL ID [" + emailId + "]",e))
                .onErrorResume(e -> buildResponse(500,"ERROR FETCHING CUSTOMER DETAILS WITH EMAIL ID [" + emailId + "]",LocalDateTime.now().toString(),null,List.of(e.getMessage()),"/customer/getCustomerByEmail/" + emailId));
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

        Mono<Customer> customerDetails = customerRepo.findByEmailId(emailId);

        Mono<Customer> customerFinalInfo = customerDetails.map(customer -> {
            customer.setAddress(customer.getAddress().stream()
                    .filter(Address::isDefaultAddress)
                    .collect(Collectors.toList()));
            return customer;
        });

        return customerFinalInfo
                .publishOn(Schedulers.parallel())
                .flatMap(customer -> buildResponse(200,"DEFAULT ADDRESS FETCHED FOR CUSTOMER WITH EMAIL ID ["+emailId+"]",LocalDateTime.now().toString(),customer.getAddress(),new ArrayList<>(),"/customer/getDefaultAddressSelectedByCustomerEmailId/" + emailId))
                        .doOnSuccess(responseMessage ->  LOGGER.info("DEFAULT ADDRESS FETCHED SUCCESSFULLY"))
                        .switchIfEmpty(Mono.defer(() -> buildResponse(404,"NO DEFAULT ADDRESS FOUND FOR CUSTOMER WITH EMAIL ID [" + emailId + "]",LocalDateTime.now().toString(),null,new ArrayList<>(),"/customer/getDefaultAddressSelectedByCustomerEmailId/" + emailId)))
                        .doOnError(e -> LOGGER.error("Error fetching customer details for email ID [" + emailId + "]", e))
                        .onErrorResume(e -> buildResponse(500,"Error fetching customer details for email ID [" + emailId + "]",LocalDateTime.now().toString(),null,List.of(e.getMessage()),"/customer/getDefaultAddressSelectedByCustomerEmailId"));
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

        return allCustomerInfo.collectList()
                .publishOn(Schedulers.parallel())
                .log("FETCHING... ALL CUSTOMERS")
                .flatMap(customers -> buildResponse(200,"ALL CUSTOMERS FETCHED SUCCESSFULLY",LocalDateTime.now().toString(),customers,new ArrayList<>(),"/customer/getAllCustomers"))
                .switchIfEmpty(Mono.defer(() -> buildResponse(404,"NO CUSTOMERS FOUND",LocalDateTime.now().toString(),null,new ArrayList<>(),"/customer/getAllCustomers")))
                .doOnSuccess(responseMessage ->LOGGER.info("ALL CUSTOMERS FETCHED SUCCESSFULLY"))
                .doOnError(e -> LOGGER.error("ERROR FETCHING ALL CUSTOMERS",e))
                .onErrorResume(e -> buildResponse(500,"ERROR FETCHING ALL CUSTOMER DETAILS",LocalDateTime.now().toString(),null,List.of(e.getMessage()),"/customer/getAllCustomers"));
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
                .then(customerRepo.deleteById(customerId))
                .then(deleteCartWhenCustomerIsDeleted(customerId))
                .then(Mono.just(true))
                .flatMap(deletionSuccess -> {
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
                .publishOn(Schedulers.parallel())
                .flatMap(customer -> {
                    Flux<Order> customerOrders = Flux.fromIterable(customer.getMyOrders());

                    return customerOrders
                            .collectList()
                            .flatMap(orders -> {
                                if (orders.isEmpty()) {
                                    return Mono.just(ResponseMessage.builder()
                                            .statusCode(404)
                                            .message("YOU HAVEN'T ORDERED ANYTHING YET")
                                            .timestamp(LocalDateTime.now().toString())
                                            .responseData(null)
                                            .errorDetails(null)
                                            .path("/order/getAllOrdersForCustomer")
                                            .build());
                                }
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


    public Mono<ResponseMessage> orderNow(String productId, OrderRequest orderRequest) throws JsonProcessingException {

        LOGGER.info("ORDER REQUEST STARTED FOR CUSTOMER ID [ " + orderRequest.getCustomerId() + "] CUSTOMER EMAIL ID: [ " +orderRequest.getCustomerEmailId()+" ] ");


        return Mono.just(new ResponseMessage());
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
                .publishOn(Schedulers.parallel())
                .flatMap(customer -> {
                    if (customer != null) {
                        customer.getMyOrders().add(customerOrder);
                        return createOrUpdateCustomer(customer)
                                .thenReturn(customer);
                    } else {
                        return Mono.error(new CustomerServiceException("Customer not found with email ID [" + customerOrder.getCustomerEmailId() + "]"));
                    }
                })
                .flatMap(updatedCustomer -> {
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(200)
                            .message("Order list updated successfully.")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(updatedCustomer.getMyOrders())
                            .errorDetails(null)
                            .path("/order/updateOrderList")
                            .build());
                })
                .onErrorResume(e -> {
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
    public Mono<ResponseMessage> addProductToCart(int customerIdForCart, CartProduct cartProduct) throws CustomerServiceException {

        LOGGER.info("ADDING PRODUCTS TO CART...");

        try{
            CartProductEvent cartProductEvent = new CartProductEvent();
            cartProductEvent.setCustomerIdForCart(customerIdForCart);
            cartProductEvent.setProductOrVariantProductIdList(cartProduct.getProductOrVariantProductIdList());
            cartProductEvent.setCartProductMessageType(CartProductEnum.ADD_CART_PRODUCT.name());

            CartProduct cartProductToAdd = getCartProduct(cartProduct);

            cartProductEvent.setCartProduct(cartProductToAdd);

            CartEvent cartEvent = new CartEvent();
            cartEvent.setCustomerIdForCart(customerIdForCart);
            cartEvent.setCartProductEvent(cartProductEvent);
            cartEvent.setCartMessageType(CartProductEnum.ADD_CART_PRODUCT.name());

            String cartProductAsMessage = objectMapper.writeValueAsString(cartEvent);

            customerKafkaProducerService.sendMessage(cartTopicName, cartProductAsMessage);

            return Mono.just(ResponseMessage.builder()
                    .statusCode(200)
                    .message("Product added to cart successfully.")
                    .timestamp(LocalDateTime.now().toString())
                    .responseData(cartProduct)
                    .errorDetails(null)
                    .path("/customer/"+customerIdForCart+"/addProductToCart/"+cartProduct.getProductOrVariantProductIdList())
                    .build());

        } catch (Exception e) {
            LOGGER.error("ERROR DURING ADDING PRODUCTS TO CART", e);

            return Mono.just(ResponseMessage.builder()
                    .statusCode(500)
                    .message("Failed to add product to cart.")
                    .timestamp(LocalDateTime.now().toString())
                    .responseData(null)
                    .errorDetails(List.of(e.getMessage()))
                    .path("/customer/"+customerIdForCart+"/addProductToCart/"+cartProduct.getProductOrVariantProductIdList())
                    .build());
        }
    }

    private static CartProduct getCartProduct(CartProduct cartProduct) {
        CartProduct cartProductToAdd = new CartProduct();
        cartProductToAdd.setProductOrVariantProductIdList(cartProduct.getProductOrVariantProductIdList());
        cartProductToAdd.setProductName(cartProduct.getProductName());
        cartProductToAdd.setProductImagesToShow(cartProduct.getProductImagesToShow());
        cartProductToAdd.setStockStatus(cartProduct.getStockStatus());
        cartProductToAdd.setListedPrice(cartProduct.getListedPrice());
        cartProductToAdd.setDiscountedPrice(cartProduct.getDiscountedPrice());
        cartProductToAdd.setQuantity(cartProduct.getQuantity());
        cartProductToAdd.setAverageRating(cartProduct.getAverageRating());
        return cartProductToAdd;
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * METHOD TO DELETE PRODUCT FROM CART
     *
     * @param customerIdForCart
     * @param productIdList
     * @return
     * @throws CustomerServiceException
     */
    public Mono<ResponseMessage> deleteProductsFromCart(int customerIdForCart, List<String> productIdList) throws CustomerServiceException, JsonProcessingException {

        LOGGER.info("REMOVING PRODUCTS FROM CART...");

        CartProductEvent cartProductEvent = new CartProductEvent();
        cartProductEvent.setCustomerIdForCart(customerIdForCart);
        cartProductEvent.setProductOrVariantProductIdList(productIdList);
        cartProductEvent.setCartProductMessageType(CartProductEnum.REMOVE_CART_PRODUCT.name());

        CartEvent cartEvent = new CartEvent();
        cartEvent.setCustomerIdForCart(customerIdForCart);
        cartEvent.setCartProductEvent(cartProductEvent);
        cartEvent.setCartMessageType(CartProductEnum.REMOVE_CART_PRODUCT.name());

        String cartProductAsMessage = objectMapper.writeValueAsString(cartEvent);

        customerKafkaProducerService.sendMessage(cartTopicName, cartProductAsMessage);

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
            LOGGER.error("TECHNICAL ISSUES DURING CART INITIATION!");
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

                    return Mono.fromRunnable(() -> {
                        customerKafkaProducerService.sendMessage(notificationTopic, customerAsMessage);
                    }).then();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    LOGGER.warn("Customer with ID {} not found", customerId);
                    return Mono.empty();
                }));
    }

    private Mono<ResponseMessage> buildResponse(
            int statusCode,
            String message,
            String timeStamp,
            Object responseData,
            List<String> errorDetails,
            String path
    ) {
        return Mono.just(ResponseMessage.builder()
                .statusCode(statusCode)
                .message(message)
                .timestamp(timeStamp)
                .responseData(responseData)
                .errorDetails(errorDetails != null ? List.of(errorDetails.toString()) : null)
                .path(path)
                .build());
    }

}