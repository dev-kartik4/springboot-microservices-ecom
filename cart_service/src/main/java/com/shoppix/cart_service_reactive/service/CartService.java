package com.shoppix.cart_service_reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppix.cart_service_reactive.entity.CartProduct;
import com.shoppix.cart_service_reactive.enums.CartEnum;
import com.shoppix.cart_service_reactive.events.CartEvent;
import com.shoppix.cart_service_reactive.exception.CartServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.shoppix.cart_service_reactive.entity.Cart;
import com.shoppix.cart_service_reactive.repo.CartRepo;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class CartService {
	
	@Autowired
	public CartRepo cartRepo;

	private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);

	public static final String PRODUCT_SERVICE_URL = "http://product-service/products";

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
	public Mono<Cart> createOrUpdateCart(Cart customerCart) {

		return cartRepo.findByCustomerIdForCart(customerCart.getCustomerIdForCart())
				.switchIfEmpty(Mono.defer(() -> {
					// If cart doesn't exist, create a new one
					LOGGER.info("No existing cart found for customer ID [" + customerCart.getCustomerIdForCart() + "], creating new cart...");
					return createNewCart(customerCart);  // Create a new cart if not found
				}))
				.flatMap(existingCart -> {
					// If the cart exists, update it
					if (existingCart.getCustomerIdForCart() > 0) {
						LOGGER.info("Existing cart found for customer ID [" + customerCart.getCustomerIdForCart() + "], updating...");
						return updateExistingCart(existingCart, customerCart);  // Update existing cart if found
					} else {
						// If it's a newly created cart, return it without update
						LOGGER.info("Cart was just created, no update needed for customer ID [" + customerCart.getCustomerIdForCart() + "]");
						return Mono.just(existingCart);  // Return the newly created cart without further updates
					}
				});
	}

	private Mono<Cart> createNewCart(Cart customerCart) {
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
					.onErrorResume(e -> {
						LOGGER.error("Error occurred while creating the cart for customer ID [" + customerCart.getCustomerIdForCart() + "]", e);
						cartEvent.setCartMessageType(CartEnum.CART_FAILED.name());
						cartKafkaProducerService.sendMessage("cart-dlt", cartAsMessage);
						return Mono.error(new CartServiceException("Technical issue while creating cart", e));
					});

		} catch (JsonProcessingException e) {
			LOGGER.error("Error serializing cart event for customer ID [" + customerCart.getCustomerIdForCart() + "]", e);
			return Mono.error(new CartServiceException("Error serializing cart event", e));
		}
	}

	private Mono<Cart> updateExistingCart(Cart existingCart, Cart updatedCart) {
		LOGGER.info("Updating existing cart for customer ID [" + updatedCart.getCustomerIdForCart() + "]");

		existingCart.setCartProducts(updatedCart.getCartProducts());
		existingCart.setTotalPrice(existingCart.getTotalPrice() + updatedCart.getTotalPrice());
		existingCart.setEventStatus(CartEnum.CART_CREATED.name());
		existingCart.setLastUpdatedDateTime(updatedCart.getLastUpdatedDateTime());

		return cartRepo.save(existingCart)
				.doOnSuccess(savedCart -> {
					LOGGER.info("Cart updated successfully for customer ID [" + savedCart.getCustomerIdForCart() + "]");
				})
				.onErrorResume(e -> {
					LOGGER.error("Failed to update cart for customer ID [" + updatedCart.getCustomerIdForCart() + "]", e);
					return Mono.error(new CartServiceException("Failed to update cart", e));
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
	public Mono<Cart> getCartDetails(int customerIdForCart) throws CartServiceException {

		LOGGER.info("FETCHED CART DETAILS FOR CUSTOMER ID ["+customerIdForCart+"]");
		Mono<Cart> cartData = cartRepo.findByCustomerIdForCart(customerIdForCart);

		return cartData.publishOn(Schedulers.parallel()).switchIfEmpty(Mono.defer(() -> {
			LOGGER.info("ERROR FETCHING CART DETAILS FOR CUSTOMER ID ["+customerIdForCart+"]");
			throw new CartServiceException("ERROR FETCHING CART DETAILS FOR CUSTOMER ID ["+customerIdForCart+"]");
		})).delaySubscription(Duration.ofMillis(3000));
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO ADD PRODUCT TO CART
	 *
	 * @param customerIdForCart
	 * @param cartProd
	 * @return
	 */
	public Mono<Cart> addProductToCart(int customerIdForCart, CartProduct cartProd){

		LOGGER.info("ADDING PRODUCTS TO CART...");

		Mono<Cart> existingCustomerCart = getCartDetails(customerIdForCart);

		Mono<Product> product = webClientBuilder.build()
				.get()
				.uri(PRODUCT_SERVICE_URL.concat("/filterProductById/"+cartProduct.getProductId()))
				.retrieve()
				.bodyToMono(Product.class)
				.subscribeOn(Schedulers.parallel());

		Flux<CartProduct> cartProducts = existingCustomerCart.flatMapIterable(Cart::getCartProducts);

		return existingCustomerCart.publishOn(Schedulers.parallel()).map(existingCart -> {
			existingCart.setCustomerIdForCart(customerIdForCart);
			Mono<CartProduct> filteredCartProduct = cartProducts.filter(cp -> cp.getProductId() == cartProd.getProductId()).single();
			Mono<CartProduct> filteredCartProductsMono = filteredCartProduct != null ? filteredCartProduct.switchIfEmpty(Mono.defer((Supplier<? extends Mono<? extends CartProduct>>) () -> {
				LOGGER.info("FINAL CART PRODUCT" + cartProd);
				existingCart.getCartProducts().add(cartProd);
				return Mono.just(cartProd);
			})) : filteredCartProduct.map(fcp -> {
				LOGGER.info("UPDATING EXISTING CART PRODUCT " + filteredCartProduct);
				CartProduct updatedCartProduct = new CartProduct();
				updatedCartProduct.setProductId(cartProd.getProductId());
				updatedCartProduct.setProductName(cartProd.getProductName());
				updatedCartProduct.setPrice(fcp.getPrice() + cartProd.getPrice());
				updatedCartProduct.setQuantity(fcp.getQuantity() + cartProd.getQuantity());
				LOGGER.info("FILTERED CART PRODUCT" + filteredCartProduct);
				existingCart.getCartProducts().add(updatedCartProduct);
				return updatedCartProduct;
			});
			existingCart.setTotalPrice(existingCart.getTotalPrice() + (cartProd.getPrice() * cartProd.getQuantity()));
			createOrUpdateCart(existingCart).subscribeOn(Schedulers.parallel());
			return existingCart;
		}).delaySubscription(Duration.ofMillis(3000));
	}

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN BOTH
	 * <p>
	 * METHOD TO DELETE PRODUCT FROM CART
	 *
	 * @param customerIdForCart
	 * @return
	 * @throws CartServiceException
	 */
	public Mono<AtomicReference<String>> deleteProductsFromCart(int customerIdForCart, int productId) throws CartServiceException{
		Mono<Cart> customerCart = getCartDetails(customerIdForCart);

		AtomicReference<String> cartProductDeleted = new AtomicReference<>("SUCCESS");

		Mono<AtomicReference<String>> productInCartMessage = customerCart.map(custoCart -> {
			custoCart.getCartProducts().stream().forEach(cartProduct -> {
				if(cartProduct.getProductId() == productId){
					custoCart.getCartProducts().remove(cartProduct);
					custoCart.setTotalPrice(custoCart.getTotalPrice()-cartProduct.getPrice());
					custoCart.setCustomerIdForCart(custoCart.getCustomerIdForCart());
					createOrUpdateCart(custoCart).subscribe();
					cartProductDeleted.set("SUCCESS");
				}
			});
			return cartProductDeleted;
		}).delaySubscription(Duration.ofMillis(3000));
		return productInCartMessage;
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
