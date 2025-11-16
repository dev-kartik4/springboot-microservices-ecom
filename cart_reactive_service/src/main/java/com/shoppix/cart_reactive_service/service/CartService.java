package com.shoppix.cart_reactive_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.cart_reactive_service.entity.CartProduct;
import com.shoppix.cart_reactive_service.enums.CartEnum;
import com.shoppix.cart_reactive_service.events.CartEvent;
import com.shoppix.cart_reactive_service.events.CartProductEvent;
import com.shoppix.cart_reactive_service.exception.CartServiceException;
import com.shoppix.cart_reactive_service.pojo.Product;
import com.shoppix.cart_reactive_service.pojo.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.shoppix.cart_reactive_service.entity.Cart;
import com.shoppix.cart_reactive_service.repo.CartRepo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Service
public class CartService {
	
	@Autowired
	public CartRepo cartRepo;

	@Autowired
	public WebClient.Builder webClientBuilder;

	private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);

	public static final String PRODUCT_SERVICE_URL = "http://product-reactive-service/api/v1/products";

    @Autowired
    private CartKafkaProducerService cartKafkaProducerService;

	private final ObjectMapper objectMapper;

	@Autowired
    public CartService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO CREATE OR SAVE NEW CART FOR FIRST TIME
	 *
	 * @param customerCart
	 * @return
	 * @throws CartServiceException
	 */
	public Mono<ResponseMessage> createOrUpdateCart(Cart customerCart) {

		return cartRepo.findByCustomerIdForCart(customerCart.getCustomerIdForCart())
				.switchIfEmpty(Mono.defer(() -> {
					LOGGER.info("No existing cart found for customer ID [" + customerCart.getCustomerIdForCart() + "], creating new cart...");
					return createNewCart(customerCart).thenReturn(customerCart);  // Create a new cart if not found
				}))
				.flatMap(existingCart -> {
					if (existingCart.getCustomerIdForCart() > 0) {
						LOGGER.info("Existing cart found for customer ID [" + customerCart.getCustomerIdForCart() + "], updating...");
						return updateExistingCart(existingCart, customerCart).thenReturn(existingCart);
					} else {
						LOGGER.info("Cart was just created, no update needed for customer ID [" + customerCart.getCustomerIdForCart() + "]");
						return Mono.just(existingCart);  // Return the newly created cart without further updates
					}
				})
				.map(cart -> {
					return ResponseMessage.builder()
							.statusCode(200)
							.message("Cart created or updated successfully.")
							.timestamp(LocalDateTime.now().toString())
							.responseData(cart)
							.errorDetails(null)
							.path("/cart/createOrUpdateCart")
							.build();
				})
				.onErrorResume(e -> {
					LOGGER.error("Error during cart creation or update", e);
					return Mono.just(ResponseMessage.builder()
							.statusCode(500)
							.message("Error during cart creation or update.")
							.timestamp(LocalDateTime.now().toString())
							.responseData(null)
							.errorDetails(List.of(e.getMessage()))
							.path("/cart/createOrUpdateCart")
							.build());
				});
	}


	private Mono<ResponseMessage> createNewCart(Cart customerCart) {
		LOGGER.info("Creating new cart for customer ID [" + customerCart.getCustomerIdForCart() + "]");

		Cart newCustomerCart = new Cart();
		newCustomerCart.setCustomerIdForCart(customerCart.getCustomerIdForCart());
		newCustomerCart.setCartProducts(customerCart.getCartProducts());
		newCustomerCart.setTotalPrice(customerCart.getTotalPrice());
		newCustomerCart.setEventStatus(CartEnum.CART_CREATED.name());
		newCustomerCart.setLastUpdatedDateTime(customerCart.getLastUpdatedDateTime());

		CartEvent cartEvent = new CartEvent();
		cartEvent.setCustomerIdForCart(customerCart.getCustomerIdForCart());
		cartEvent.setCartMessage(customerCart);

		try {
			String cartAsMessage = objectMapper.writeValueAsString(cartEvent);
			cartEvent.setCartMessageType(CartEnum.CART_CREATED.name());

			return cartRepo.insert(newCustomerCart)
					.doOnSuccess(savedCart -> {
						LOGGER.info("Cart created successfully for customer ID [" + savedCart.getCustomerIdForCart() + "]");
						cartKafkaProducerService.sendMessage("cart-topic", cartAsMessage);
					})
					.map(savedCart -> {
						return ResponseMessage.builder()
								.statusCode(200)
								.message("Cart created successfully.")
								.timestamp(LocalDateTime.now().toString())
								.responseData(savedCart)
								.errorDetails(null)
								.path("/cart/createOrUpdateCart/customer")
								.build();
					})
					.onErrorResume(e -> {
						LOGGER.error("Error occurred while creating the cart for customer ID [" + customerCart.getCustomerIdForCart() + "]", e);
						cartEvent.setCartMessageType(CartEnum.CART_FAILED.name());
						cartKafkaProducerService.sendMessage("cart-dlt", cartAsMessage);
						return Mono.just(ResponseMessage.builder()
								.statusCode(500)
								.message("Error creating cart.")
								.timestamp(LocalDateTime.now().toString())
								.responseData(null)
								.errorDetails(List.of(e.getMessage()))
								.path("/cart/createOrUpdateCart/customer")
								.build());
					});

		} catch (JsonProcessingException e) {
			LOGGER.error("Error serializing cart event for customer ID [" + customerCart.getCustomerIdForCart() + "]", e);
			return Mono.just(ResponseMessage.builder()
					.statusCode(500)
					.message("Error serializing cart event.")
					.timestamp(LocalDateTime.now().toString())
					.responseData(null)
					.errorDetails(List.of(e.getMessage()))
					.path("/cart/createOrUpdateCart/customer")
					.build());
		}
	}


	private Mono<ResponseMessage> updateExistingCart(Cart existingCart, Cart updatedCart) {
		LOGGER.info("Updating existing cart for customer ID [" + updatedCart.getCustomerIdForCart() + "]");

		existingCart.setCartProducts(updatedCart.getCartProducts());
		existingCart.setTotalPrice(existingCart.getTotalPrice() + updatedCart.getTotalPrice());
		existingCart.setEventStatus(CartEnum.CART_UPDATED.name());
		existingCart.setLastUpdatedDateTime(LocalDateTime.now().toString());

		return cartRepo.save(existingCart)
				.doOnSuccess(savedCart -> {
					LOGGER.info("Cart updated successfully for customer ID [" + savedCart.getCustomerIdForCart() + "]");
				})
				.map(savedCart -> {
					return ResponseMessage.builder()
							.statusCode(200)
							.message("Cart updated successfully.")
							.timestamp(LocalDateTime.now().toString())
							.responseData(savedCart)
							.errorDetails(null)
							.path("/cart/createOrUpdateCart/customer")
							.build();
				})
				.onErrorResume(e -> {
					LOGGER.error("Failed to update cart for customer ID [" + updatedCart.getCustomerIdForCart() + "]", e);
					return Mono.just(ResponseMessage.builder()
							.statusCode(500)
							.message("Failed to update cart.")
							.timestamp(LocalDateTime.now().toString())
							.responseData(null)
							.errorDetails(List.of(e.getMessage()))
							.path("/cart/createOrUpdateCart/customer")
							.build());
				});
	}




	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO FETCH CART DETAILS
	 *
	 * @param customerIdForCart
	 * @return
	 * @throws CartServiceException
	 */
	public Mono<ResponseMessage> getCartDetails(int customerIdForCart) throws CartServiceException {

		LOGGER.info("FETCHED CART DETAILS FOR CUSTOMER ID ["+customerIdForCart+"]");
		Mono<Cart> cartData = cartRepo.findByCustomerIdForCart(customerIdForCart);

        return cartData
                .publishOn(Schedulers.parallel())
                .flatMap(customer -> {
                    if (customer == null) {
                        LOGGER.info("CART DETAILS NOT FOUND FOR CUSTOMER ID ["+customerIdForCart+"]");
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(404)
                                .message("CART DETAILS NOT FOUND FOR CUSTOMER ID ["+customerIdForCart+"]")
                                .timestamp(LocalDateTime.now().toString())
                                .responseData(null)
                                .errorDetails(List.of("CART DETAILS FOR CUSTOMER ID ["+customerIdForCart+"] DOES NOT EXIST"))
                                .path("/cart/viewCart/customer/"+customerIdForCart)
                                .build());
                    } else {
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(200)
                                .message("CART DETAILS FETCHED SUCCESSFULLY")
                                .timestamp(LocalDateTime.now().toString())
                                .responseData(cartData)
                                .errorDetails(null)
                                .path("/cart/viewCart/customer/"+customerIdForCart)
                                .build());
                    }
                })
                .onErrorResume(e -> {
                    LOGGER.error("ERROR FETCHING CART DETAILS", e);
                    return Mono.just(ResponseMessage.builder()
                            .statusCode(500)
                            .message("ERROR FETCHING CART DETAILS")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(null)
                            .errorDetails(List.of(e.getMessage()))
                            .path("/cart/viewCart/customer/"+customerIdForCart)
                            .build());

                });
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO ADD PRODUCT TO CART
	 *
	 * @param cartProductEvent
	 * @return
	 */

    public Mono<ResponseMessage> addProductToCart(CartProductEvent cartProductEvent) throws CartServiceException {

        LOGGER.info("ADDING PRODUCTS TO CART");

        Mono<CartProduct> newCartProductMono =webClientBuilder.build()
                .get()
                .uri(PRODUCT_SERVICE_URL.concat("/filterProductById/"+cartProductEvent.getProductOrVariantProductIdList().stream().findFirst().get()))
                .retrieve()
                .bodyToMono(Product.class)
                .map(product -> {
                    CartProduct cartProduct = cartProductEvent.getCartProduct();
//                    cartProduct.setProductOrVariantProductIdList(cartProduct.getProductOrVariantProductIdList());
                    cartProduct.setProductName(product.getProductName());
                    product.getProductVariations().forEach(variation -> {
                        cartProduct.setProductOrVariantProductIdList(Collections.singletonList(variation.getVariantProductId()));
                        cartProduct.setProductImagesToShow(variation.getProductImages());
                        cartProduct.setAverageRating(variation.getAverageRating());
                        cartProduct.setListedPrice(variation.getListingPrice());
                        cartProduct.setDiscountedPrice(variation.getDiscountedPrice());
                        cartProduct.setStockStatus(variation.getProductAvailabilityStatus());
                        cartProduct.setAverageRating(variation.getAverageRating());
                    });
                    LOGGER.info("FETCHING PRODUCTS TO CART SUCCESSFUL: {}",cartProduct);
                    return cartProduct;
                })
                .onErrorResume(e -> {
                    LOGGER.error("ERROR FETCHING PRODUCTS TO CART", e);
                    return Mono.empty();
                }).subscribeOn(Schedulers.parallel());

        return newCartProductMono.flatMap(newCartProduct -> {
            return cartRepo.findByCustomerIdForCart(cartProductEvent.getCustomerIdForCart())
                    .flatMap(existingCart -> {
                        return Flux.fromIterable(existingCart.getCartProducts())
                                .filter(product -> product.getProductOrVariantProductIdList().equals(newCartProduct.getProductOrVariantProductIdList()))
                                .singleOrEmpty()
                                .publishOn(Schedulers.parallel())
                                .flatMap(existingCartProduct -> {
                                    existingCartProduct.setProductOrVariantProductIdList(newCartProduct.getProductOrVariantProductIdList());
                                    existingCartProduct.setProductName(newCartProduct.getProductName());
                                    existingCartProduct.setAverageRating(newCartProduct.getAverageRating());
                                    existingCartProduct.setProductImagesToShow(newCartProduct.getProductImagesToShow());
                                    existingCartProduct.setStockStatus(newCartProduct.getStockStatus());
                                    existingCartProduct.setQuantity(existingCartProduct.getQuantity() + newCartProduct.getQuantity());
                                    existingCartProduct.setListedPrice(newCartProduct.getListedPrice());
                                    existingCartProduct.setDiscountedPrice(existingCartProduct.getDiscountedPrice() + newCartProduct.getDiscountedPrice());
                                    existingCart.getCartProducts().add(existingCartProduct);
                                    LOGGER.info("UPDATED EXISTING CART PRODUCT: {}", existingCartProduct);
                                    return cartRepo.save(existingCart);
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    existingCart.getCartProducts().add(newCartProduct);
                                    LOGGER.info("ADDING NEW PRODUCT TO CART: {}", newCartProduct);
                                    return cartRepo.save(existingCart);
                                }))
                                .doOnTerminate(() -> {
                                    LOGGER.info("TERMINATE METHOD CALLED: {}", existingCart);
                                    existingCart.setTotalPrice(existingCart.getTotalPrice() + (newCartProduct.getDiscountedPrice() * newCartProduct.getQuantity()));
                                    createOrUpdateCart(existingCart).subscribe();
                                });
                            });

                    })
                    .map(updatedCart -> ResponseMessage.builder()
                            .statusCode(200)
                            .message("PRODUCT ADDED TO CART SUCCESSFULLY")
                            .timestamp(LocalDateTime.now().toString())
                            .responseData(updatedCart)
                            .errorDetails(List.of())
                            .path("/cart/addProductToCart")
                            .build())
                    .onErrorResume(e -> {
                        LOGGER.error("ERROR ADDING PRODUCT TO CART", e);
                        return Mono.just(ResponseMessage.builder()
                                .statusCode(500)
                                .message("ERROR ADDING PRODUCT TO CART")
                                .errorDetails(List.of(e.getMessage()))
                                .path("/cart/addProductToCart")
                                .timestamp(LocalDateTime.now().toString())
                                .build());
                    });
        }


	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO DELETE PRODUCT FROM CART
	 *
	 * @param cartProductEvent
	 * @return
	 * @throws CartServiceException
	 */
	public Mono<ResponseMessage> removeProductsFromCart(CartProductEvent cartProductEvent) {

		return cartRepo.findByCustomerIdForCart(cartProductEvent.getCustomerIdForCart())
				.flatMap(cart -> {

					List<CartProduct> productsToRemove = cart.getCartProducts().stream()
							.filter(cartProduct -> cartProductEvent.getProductOrVariantProductIdList().equals(cartProduct.getProductOrVariantProductIdList()))
							.toList();

					if (!productsToRemove.isEmpty()) {

						cart.getCartProducts().removeAll(productsToRemove);

						double totalPriceReduction = productsToRemove.stream()
								.mapToDouble(product -> product.getDiscountedPrice() * product.getQuantity())
								.sum();
						cart.setTotalPrice(cart.getTotalPrice() - totalPriceReduction);

						return cartRepo.save(cart)
								.map(savedCart -> ResponseMessage.builder()
										.statusCode(200)
										.message("Products removed from cart successfully")
										.timestamp(System.currentTimeMillis() + "")
										.responseData(savedCart)
										.errorDetails(null)
										.path("/api/v1/cart")
										.build());
					} else {
						return Mono.just(ResponseMessage.builder()
								.statusCode(404)
								.message("No matching products found in cart")
								.timestamp(System.currentTimeMillis() + "")
								.responseData(null)
								.errorDetails(List.of("None of the products in the provided list were found"))
								.path("/api/v1/cart")
								.build());
					}
				})
				.onErrorResume(e -> Mono.just(ResponseMessage.builder()
						.statusCode(500)
						.message("Error removing products from cart")
						.timestamp(System.currentTimeMillis() + "")
						.responseData(null)
						.errorDetails(List.of(e.getMessage()))
						.path("/api/v1/cart")
						.build()));
	}


	@Transactional
	public Mono<ResponseEntity<String>> deleteCartWhenCustomerIsDeleted( int customerIdForCart) {
		LOGGER.info("FETCHING.. INFO TO DELETE CART WITH CUSTOMER ID [" + customerIdForCart + "]");
		return cartRepo.deleteById(customerIdForCart)
				.then().log("CART SUCCESSFULLY DELETED FOR CUSTOMER ID")
				.then(Mono.just(new ResponseEntity<>(
						"CART SUCCESSFULLY DELETED FOR CUSTOMER ID [" + customerIdForCart + "]",
						HttpStatus.ACCEPTED)))
				.onErrorResume(e -> {
					LOGGER.error("Error deleting cart with ID [{}]", customerIdForCart, e);
					return Mono.error(new CartServiceException("FAILED TO DELETE CART WITH CUSTOMER ID ["+customerIdForCart+"]"));
				});
	}

}
