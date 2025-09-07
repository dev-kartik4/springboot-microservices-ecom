package com.shoppix.cart_reactive_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.cart_reactive_service.entity.CartProduct;
import com.shoppix.cart_reactive_service.enums.CartEnum;
import com.shoppix.cart_reactive_service.events.CartEvent;
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
import java.util.List;


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
					// If cart doesn't exist, create a new one
					LOGGER.info("No existing cart found for customer ID [" + customerCart.getCustomerIdForCart() + "], creating new cart...");
					return createNewCart(customerCart).thenReturn(customerCart);  // Create a new cart if not found
				}))
				.flatMap(existingCart -> {
					// If the cart exists, update it
					if (existingCart.getCustomerIdForCart() > 0) {
						LOGGER.info("Existing cart found for customer ID [" + customerCart.getCustomerIdForCart() + "], updating...");
						return updateExistingCart(existingCart, customerCart)  // Update existing cart if found
								.thenReturn(existingCart); // After update, return the existing cart
					} else {
						// If it's a newly created cart, return it without update
						LOGGER.info("Cart was just created, no update needed for customer ID [" + customerCart.getCustomerIdForCart() + "]");
						return Mono.just(existingCart);  // Return the newly created cart without further updates
					}
				})
				.map(cart -> {
					// Return a success response with the cart in the responseData
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
					// Return an error response if something goes wrong
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
						// Successful response
						return ResponseMessage.builder()
								.statusCode(200)
								.message("Cart created successfully.")
								.timestamp(LocalDateTime.now().toString())
								.responseData(savedCart)
								.errorDetails(null)
								.path("/cart/createNewCart")
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
								.path("/cart/createNewCart")
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
					.path("/cart/createNewCart")
					.build());
		}
	}


	private Mono<ResponseMessage> updateExistingCart(Cart existingCart, Cart updatedCart) {
		LOGGER.info("Updating existing cart for customer ID [" + updatedCart.getCustomerIdForCart() + "]");

		existingCart.setCartProducts(updatedCart.getCartProducts());
		existingCart.setTotalPrice(existingCart.getTotalPrice() + updatedCart.getTotalPrice());
		existingCart.setEventStatus(CartEnum.CART_CREATED.name());
		existingCart.setLastUpdatedDateTime(updatedCart.getLastUpdatedDateTime());

		return cartRepo.save(existingCart)
				.doOnSuccess(savedCart -> {
					LOGGER.info("Cart updated successfully for customer ID [" + savedCart.getCustomerIdForCart() + "]");
				})
				.map(savedCart -> {
					// Successful response
					return ResponseMessage.builder()
							.statusCode(200)
							.message("Cart updated successfully.")
							.timestamp(LocalDateTime.now().toString())
							.responseData(savedCart)
							.errorDetails(null)
							.path("/cart/updateExistingCart")
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
							.path("/cart/updateExistingCart")
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

		return Mono.just(new ResponseMessage());

	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO ADD PRODUCT TO CART
	 *
	 * @param customerIdForCart
	 * @param cartProduct
	 * @return
	 */
//	public Mono<ResponseMessage> addProductToCart(int customerIdForCart, String productId, CartProduct cartProduct) {
//		LOGGER.info("ADDING PRODUCTS TO CART...");
//
//		// Fetching the existing cart details for the customer
//		Mono<Cart> existingCustomerCart = getCartDetails(customerIdForCart);
//
//		// Fetching the product details for the provided product ID
//		Mono<Product> product = webClientBuilder.build()
//				.get()
//				.uri(PRODUCT_SERVICE_URL.concat("/filterProductById/" + productId))
//				.retrieve()
//				.bodyToMono(Product.class)
//				.subscribeOn(Schedulers.parallel());
//
//		// Fetching the cart products and filtering based on productId
//		Flux<CartProduct> cartProducts = existingCustomerCart.flatMapIterable(Cart::getCartProducts);
//
//		return existingCustomerCart.publishOn(Schedulers.parallel()).map(existingCart -> {
//
//					existingCart.setCustomerIdForCart(customerIdForCart);
//
//					// Find if the product is already in the cart, else add it
//					Mono<CartProduct> filteredCartProduct = cartProducts.filter(cp -> cp.getProductId().equals(cartProduct.getProductId())).single();
//
//					return filteredCartProduct.switchIfEmpty(Mono.defer(() -> {
//								// If the product is not found in the cart, add it
//								LOGGER.info("FINAL CART PRODUCT: " + cartProduct);
//								existingCart.getCartProducts().add(cartProduct);
//								return Mono.just(cartProduct);
//							}))
//							.map(filteredProduct -> {
//								// If the product is found, update its quantity and price
//								LOGGER.info("UPDATING EXISTING CART PRODUCT: " + filteredProduct);
//								CartProduct updatedCartProduct = new CartProduct();
//								updatedCartProduct.setProductId(cartProduct.getProductId());
//								updatedCartProduct.setProductName(cartProduct.getProductName());
//								updatedCartProduct.setPrice(filteredProduct.getPrice() + cartProduct.getPrice());
//								updatedCartProduct.setQuantity(filteredProduct.getQuantity() + cartProduct.getQuantity());
//
//								// Add the updated product to the cart
//								existingCart.getCartProducts().add(updatedCartProduct);
//								return updatedCartProduct;
//							})
//							.doOnTerminate(() -> {
//								// Update total price of the cart after adding/updating the product
//								existingCart.setTotalPrice(existingCart.getTotalPrice() + (cartProduct.getPrice() * cartProduct.getQuantity()));
//								createOrUpdateCart(existingCart).subscribeOn(Schedulers.parallel()); // Persist updated cart
//							});
//
//				}).flatMap(updatedCart -> {
//					// Return success response with the updated cart data
//					return Mono.just(ResponseMessage.builder()
//							.statusCode(200)
//							.message("Product added to cart successfully.")
//							.timestamp(LocalDateTime.now().toString())
//							.responseData(updatedCart)
//							.errorDetails(null)
//							.path("/cart/addProductToCart")
//							.build());
//				})
//				.onErrorResume(e -> {
//					// In case of any error, return an error response with details
//					LOGGER.error("Error adding product to cart for customer ID [" + customerIdForCart + "]", e);
//					return Mono.just(ResponseMessage.builder()
//							.statusCode(500)
//							.message("Error adding product to cart.")
//							.timestamp(LocalDateTime.now().toString())
//							.responseData(null)
//							.errorDetails(List.of(e.getMessage()))
//							.path("/cart/addProductToCart")
//							.build());
//				})
//				.delaySubscription(Duration.ofMillis(3000));  // Optional delay if needed
//	}


	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO DELETE PRODUCT FROM CART
	 *
	 * @param customerIdForCart
	 * @return
	 * @throws CartServiceException
	 */
	public Mono<ResponseMessage> removeProductsFromCart(int customerIdForCart, List<String> productIds) {
		return cartRepo.findByCustomerIdForCart(customerIdForCart) // Get cart by customer ID
				.flatMap(cart -> {
					// Filter the products to remove from the cart
					List<CartProduct> productsToRemove = cart.getCartProducts().stream()
							.filter(cartProduct -> productIds.contains(cartProduct.getProductId()))
							.toList();

					if (!productsToRemove.isEmpty()) {
						// Remove the products from the cart
						cart.getCartProducts().removeAll(productsToRemove);

						// Recalculate the total price
						double totalPriceReduction = productsToRemove.stream()
								.mapToDouble(product -> product.getPrice() * product.getQuantity())
								.sum();
						cart.setTotalPrice(cart.getTotalPrice() - totalPriceReduction);

						// Save the updated cart
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
						// Return a response indicating no products were found to remove
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
