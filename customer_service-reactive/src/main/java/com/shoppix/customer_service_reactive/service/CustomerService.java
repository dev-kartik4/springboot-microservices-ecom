package com.shoppix.customer_service_reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.customer_service_reactive.entity.Address;
import com.shoppix.customer_service_reactive.entity.Customer;
import com.shoppix.customer_service_reactive.enums.CustomerEnum;
import com.shoppix.customer_service_reactive.events.CartEvent;
import com.shoppix.customer_service_reactive.events.CustomerEvent;
import com.shoppix.customer_service_reactive.exception.CustomerServiceException;
import com.shoppix.customer_service_reactive.model.Cart;
import com.shoppix.customer_service_reactive.model.CartProduct;
import com.shoppix.customer_service_reactive.model.Order;
import com.shoppix.customer_service_reactive.repo.CustomerRepo;
import com.shoppix.customer_service_reactive.util.CustomerUtil;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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

    public static final String CART_SERVICE_URL = "http://cart-service/cart";

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
    public Mono<Customer> createOrUpdateCustomer(Customer customer) {

        LOGGER.info("FETCHING CUSTOMER EXISTENCE...");

        return customerRepo.findByEmailId(customer.getEmailId())
                .switchIfEmpty(Mono.defer(() -> createNewCustomer(customer)))
                .flatMap(existingCustomer -> updateExistingCustomer(existingCustomer, customer));
    }

    private Mono<Customer> createNewCustomer(Customer customer) {
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
        cart.setCartProducts(new LinkedList<>());

        return customerRepo.insert(newCustomer).subscribeOn(Schedulers.parallel())
                .doOnSuccess(savedCustomer -> {
                    LOGGER.info("CUSTOMER AND THEIR CART CREATED SUCCESSFULLY");
                    try {
                        cart.setEventStatus(CustomerEnum.CUSTOMER_REGISTERED.name());
                        cart.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
                        createOrUpdateCartForCustomer(cart);
                        sendNotificationToCustomer(CustomerEnum.CUSTOMER_REGISTERED.name(),newCustomer.getCustomerId())
                                .doOnTerminate(() -> LOGGER.info("Process completed"))
                                .subscribe();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .onErrorResume(e -> {
                    newCustomer.setEventStatus(CustomerEnum.CUSTOMER_DELETED.name());
                    newCustomer.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
                    newCustomer.setAccountExistence(false);
                    customerRepo.save(newCustomer).subscribe();
                    LOGGER.error("OOPS TECHNICAL ERROR! NEW CUSTOMER REGISTRATION FAILED", e);
                    return Mono.error(new CustomerServiceException("OOPS TECHNICAL ERROR! NEW CUSTOMER REGISTRATION FAILED", e));
                });
    }

    private Mono<Customer> updateExistingCustomer(Customer existingCustomer, Customer updatedCustomer) {
        LOGGER.info("UPDATING EXISTING CUSTOMER...");

        existingCustomer.setCustomerId(existingCustomer.getCustomerId());
        existingCustomer.setCustomerName(updatedCustomer.getCustomerName());
        existingCustomer.setEmailId(updatedCustomer.getEmailId());
        existingCustomer.setAddress(updatedCustomer.getAddress());
        existingCustomer.setPassword(updatedCustomer.getPassword());
        existingCustomer.setPhone(updatedCustomer.getPhone());
        existingCustomer.getMyOrders().addAll(updatedCustomer.getMyOrders());
        existingCustomer.setEventStatus(CustomerEnum.CUSTOMER_REGISTERED.name());
        existingCustomer.setCreatedDateTime(existingCustomer.getCreatedDateTime());
        existingCustomer.setLastUpdatedDateTime(generateLastUpdatedDateTime(new Date()));
        existingCustomer.setAccountExistence(true);

        // Save the updated customer
        return customerRepo.save(existingCustomer)
                .doOnSuccess(savedCustomer -> LOGGER.info("CUSTOMER UPDATED SUCCESSFULLY"))
                .onErrorResume(e -> {
                    LOGGER.error("Failed to update customer", e);
                    return Mono.error(new CustomerServiceException("Failed to update customer", e));
                });
    }

    public Mono<Customer> addYourNewAddress(String emailId, Address address) throws CustomerServiceException{

        LOGGER.info("UPDATING NEW ADDRESS DETAILS FOR CUSTOMER [" +emailId+"]");
        Mono<Customer> existingCustomerData = customerRepo.findByEmailId(emailId);
        Mono<Customer> updatedCustomerData =  existingCustomerData.publishOn(Schedulers.parallel()).map(updatedCustomer -> {
            updatedCustomer.getAddress().add(address);
            customerRepo.save(updatedCustomer).subscribe();
            return updatedCustomer;
        });

        return updatedCustomerData;
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
    public Mono<Customer> getCustomerById(int customerId) throws CustomerServiceException {

        LOGGER.info("FETCHING CUSTOMER DETAILS WITH CUSTOMER ID ["+customerId+"]...");

        Mono<Customer> customerMono = customerRepo.findById(customerId);

        return customerMono.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.empty());
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
    public Mono<Customer> getCustomerByEmail(String emailId) throws CustomerServiceException {

        Mono<Customer> customerMono = customerRepo.findByEmailId(emailId);

        LOGGER.info("FETCHING CUSTOMER DETAILS WITH CUSTOMER EMAIL ID ["+emailId+"]...");

        LOGGER.info("CUSTOMER BODY OBJECT ["+customerMono.toString()+"]");
        return customerMono.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.error(() -> {
                    LOGGER.error("ERROR FETCHING CUSTOMER DETAILS WITH CUSTOMER ID [" + emailId + "]");
                    throw new CustomerServiceException("ERROR FETCHING CUSTOMER DETAILS WITH CUSTOMER ID [" + emailId + "]");
                }
        ));
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
    public Mono<Customer> getDefaultAddressSelectedByCustomerEmailId(String emailId) throws CustomerServiceException {

        LOGGER.info("SERVICE ADDRESS");
        Mono<Customer> customerDetails = getCustomerByEmail(emailId);

        Mono<Customer> customerFinalInfo = customerDetails.publishOn(Schedulers.parallel()).map(customer -> {
            customer.setAddress(customer.getAddress().stream().filter(Address::isDefaultAddress).toList());
            return customer;
        });
        LOGGER.info("ADDRESS "+customerDetails);

        return customerFinalInfo.switchIfEmpty(Mono.error(() -> {
                LOGGER.error("ERROR FETCHING CUSTOMER ADDRESS WITH CUSTOMER EMAIL ID [" + emailId + "]");
                throw new CustomerServiceException("ERROR FETCHING CUSTOMER ADDRESS WITH CUSTOMER EMAIL ID [" + emailId + "]");
                }
        ));
    }

    /**
     * WILL BE CONTROLLED BY ADMIN
     * <p>
     * METHOD TO FETCH LIST OF CUSTOMERS REGISTERED WITH US
     *
     * @return
     * @throws CustomerServiceException
     */
    public Flux<Customer> getAllCustomers() throws CustomerServiceException {

        Flux<Customer> allCustomerInfo = customerRepo.findAll();

        return allCustomerInfo.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.error(() -> {
            LOGGER.error("ERROR FETCHING ALL CUSTOMER INFO");
            throw new CustomerServiceException("ERROR FETCHING ALL CUSTOMER INFO");
        }));
    }

    /**
     * WILL BE CONTROLLED BY ADMIN ONLY
     * <p>
     * METHOD TO DELETE CUSTOMER ACCOUNT BY CUSTOMER ID UPON INACTIVITY FOR LONG TIME
     *
     * @param customerId
     * @throws CustomerServiceException
     */
    public Mono<Boolean> deleteCustomerById(int customerId) {
        LOGGER.info("IN PROCESS OF DELETING PROFILE WITH CUSTOMER ID [" + customerId + "]");

        return sendNotificationToCustomer(CustomerEnum.CUSTOMER_DELETED.name(), customerId)
                .then(customerRepo.deleteById(customerId)) // Delete customer by ID
                .then(deleteCartWhenCustomerIsDeleted(customerId)) // Delete customer cart (if needed)
                .then(Mono.just(true)) // Final step, returning true
                .onErrorResume(e -> { // If any error occurs, log and return false
                    LOGGER.error("Error during customer deletion process", e);
                    return Mono.just(false);
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
    public Flux<Order> updateOrderList(Order customerOrder) throws CustomerServiceException {

        Mono<Customer> customerData = getCustomerByEmail(customerOrder.getCustomerEmailId());

        customerData.publishOn(Schedulers.parallel()).map(customer -> {
            if(customer != null){
                LOGGER.info("UPDATING ORDER LIST FOR CUSTOMER WITH EMAIL ID [" + customerOrder.getCustomerEmailId() + "]");
                customer.getMyOrders().add(customerOrder);
                createOrUpdateCustomer(customer);
            }
            return customer;
        }).switchIfEmpty(Mono.defer(() -> {
            LOGGER.error("ERROR UPDATING ORDER LIST FOR CUSTOMER WITH CUSTOMER EMAIL ID [" + customerOrder.getCustomerEmailId() + "]");
            throw new CustomerServiceException("ERROR UPDATING ORDER LIST FOR CUSTOMER WITH CUSTOMER EMAIL ID [" + customerOrder.getCustomerEmailId() + "]");
        }));

        return Flux.from(customerData.flatMapIterable(Customer::getMyOrders)).delaySubscription(Duration.ofMillis(3000));
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
    public Flux<Order> getAllOrdersForCustomer(String emailId) {

        Mono<Customer> completeCustomerInfo = getCustomerByEmail(emailId);
        LOGGER.info("FETCHING... YOUR RECENT ORDERS");
        Flux<Order> customerOrders = completeCustomerInfo.publishOn(Schedulers.parallel()).flatMapIterable(Customer::getMyOrders);

        return customerOrders.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.error(() -> {
            LOGGER.error("YOU HAVEN'T ORDERED ANYTHING YET");
            throw new CustomerServiceException("YOU HAVEN'T ORDERED ANYTHING YET");
        }));
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
    public Mono<Cart> addProductToCart(int customerIdForCart, CartProduct cartProduct){

        LOGGER.info("ADDING PRODUCTS TO CART...");

        Mono<Cart> existingCustomerCart = webClientBuilder.build()
                .get()
                .uri(CART_SERVICE_URL.concat("/viewCart/customer/").concat(String.valueOf(customerIdForCart)))
                .retrieve()
                .bodyToMono(Cart.class)
                .publishOn(Schedulers.parallel());

        Mono<Product> product = webClientBuilder.build()
                .get()
                    .uri(PRODUCT_SERVICE_URL.concat("/filterProductById/"+cartProduct.getProductId()))
                        .retrieve()
                            .bodyToMono(Product.class)
                                .subscribeOn(Schedulers.parallel());

        Flux<CartProduct> cartProducts = existingCustomerCart.flatMapIterable(Cart::getCartProducts);

        return existingCustomerCart.publishOn(Schedulers.parallel()).map(existingCart -> {
            existingCart.setCustomerIdForCart(customerIdForCart);
            product.map(prod-> {
                CartProduct cartProductToAdd = new CartProduct();
                cartProductToAdd.setProductId(prod.getProductId());
                cartProductToAdd.setProductName(prod.getProductName());

                cartProductToAdd.setPrice(prod.getPrice());
                cartProductToAdd.setStockStatus(prod.getStockStatus());
                cartProductToAdd.setQuantity(cartProduct.getQuantity());

                Mono<CartProduct> filteredCartProduct = cartProducts.filter(cp -> cp.getProductId() == cartProduct.getProductId()).single();
                Mono<CartProduct> filteredCartProductsMono = filteredCartProduct != null ? filteredCartProduct.switchIfEmpty(Mono.defer((Supplier<? extends Mono<? extends CartProduct>>) () -> {
                    LOGGER.info("FINAL CART PRODUCT" + cartProduct);
                    existingCart.getCartProducts().add(cartProduct);
                    return Mono.just(cartProduct);
                })) : filteredCartProduct.map(fcp -> {
                    LOGGER.info("UPDATING EXISTING CART PRODUCT " + filteredCartProduct);
                    CartProduct updatedCartProduct = new CartProduct();
                    updatedCartProduct.setProductId(cartProduct.getProductId());
                    updatedCartProduct.setProductName(cartProduct.getProductName());
                    updatedCartProduct.setPrice(fcp.getPrice() + cartProduct.getPrice());
                    updatedCartProduct.setQuantity(fcp.getQuantity() + cartProduct.getQuantity());
                    LOGGER.info("FILTERED CART PRODUCT" + filteredCartProduct);
                    existingCart.getCartProducts().add(updatedCartProduct);
                    return updatedCartProduct;
                });
                existingCart.setTotalPrice((int) (existingCart.getTotalPrice() + (cartProduct.getPrice() * cartProduct.getQuantity())));
                try {
                    createOrUpdateCartForCustomer(existingCart);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                return cartProductToAdd;
            });
            return existingCart;
        });
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
    public Mono<AtomicReference<String>> deleteProductsFromCart(int customerIdForCart, int productId) throws CustomerServiceException{
        Mono<Cart> customerCart = webClientBuilder.build()
                .get()
                .uri(CART_SERVICE_URL.concat("/viewCart/customer/").concat(String.valueOf(customerIdForCart)))
                .retrieve()
                .bodyToMono(Cart.class)
                .publishOn(Schedulers.parallel());

        AtomicReference<String> cartProductDeleted = new AtomicReference<>("SUCCESS");

        Mono<AtomicReference<String>> productInCartMessage = customerCart.map(custoCart -> {
            custoCart.getCartProducts().stream().forEach(cartProduct -> {
                if(cartProduct.getProductId() == productId){
                    custoCart.getCartProducts().remove(cartProduct);
                    custoCart.setTotalPrice((int) (custoCart.getTotalPrice()-cartProduct.getPrice()));
                    custoCart.setCustomerIdForCart(custoCart.getCustomerIdForCart());
                    try {
                        createOrUpdateCartForCustomer(custoCart);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    cartProductDeleted.set("SUCCESS");
                }
            });
            return cartProductDeleted;
        });
        return productInCartMessage;
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
            customerRepo.deleteById(customerId);
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
