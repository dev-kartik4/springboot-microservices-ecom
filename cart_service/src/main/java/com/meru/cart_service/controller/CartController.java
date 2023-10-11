package com.meru.cart_service.controller;

import com.meru.cart_service.entity.Cart;
import com.meru.cart_service.entity.CartProducts;
import com.meru.cart_service.exception.CartServiceException;
import com.meru.cart_service.service.CartService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@CrossOrigin("*")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    public CartService cartService;

    @Autowired
    public RestTemplate restTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);

    private static final String CART_SERVICE = "cartService";
    @PostMapping("/createCart/customer/{customerIdForCart}")
    @CircuitBreaker(name = CART_SERVICE,fallbackMethod = "cartFallback")
    @Retry(name = CART_SERVICE,fallbackMethod = "cartFallback")
    @RateLimiter(name = CART_SERVICE,fallbackMethod = "cartFallback")
    @ResponseBody
    public ResponseEntity<Cart> createCart(@PathVariable int customerIdForCart,@RequestBody Cart cart){

        Cart customerCart = cartService.createOrSaveCart(cart);
        return new ResponseEntity(customerCart,HttpStatus.OK);
    }

    @GetMapping("/viewCart/customer/{customerIdForCart}")
    @CircuitBreaker(name = CART_SERVICE,fallbackMethod = "cartFallback")
    @Retry(name = CART_SERVICE,fallbackMethod = "cartFallback")
    @RateLimiter(name = CART_SERVICE,fallbackMethod = "cartFallback")
    public ResponseEntity<Cart> getCartDetails(@PathVariable int customerIdForCart) throws CartServiceException{
        Optional<Cart> cart = cartService.getCartDetails(customerIdForCart);

        if(cart.isPresent()){
            return new ResponseEntity(cart,HttpStatus.OK);
        }else{
            throw new CartServiceException("ERROR FETCHING CART DETAILS ! PLEASE CHECK IF YOU HAVE LOGGED IN");
        }

    }

    @PutMapping("/customer/{customerIdForCart}/addProductToCart/{productId}")
    @CircuitBreaker(name = CART_SERVICE,fallbackMethod = "cartFallback")
    @Retry(name = CART_SERVICE,fallbackMethod = "cartFallback")
    @RateLimiter(name = CART_SERVICE,fallbackMethod = "cartFallback")
    public ResponseEntity<Cart> addProductToCart(@PathVariable int customerIdForCart,@RequestBody CartProducts cartProd) throws CartServiceException{
        Optional<Cart> existingCustomerCart = cartService.getCartDetails(customerIdForCart);
        Set<CartProducts> cartProducts = existingCustomerCart.get().cartProducts;

        if (existingCustomerCart.isPresent()) {
            existingCustomerCart.get().setCustomerIdForCart(customerIdForCart);
            Optional<CartProducts> filteredCartProduct = cartProducts.stream().filter(cp -> cp.getProductId() == cartProd.getProductId()).findFirst();
            if(filteredCartProduct.isEmpty()){
                LOGGER.info("FINAL CART PRODUCT"+cartProd);
                existingCustomerCart.get().getCartProducts().add(cartProd);
            }else{
                LOGGER.info("UPDATING EXISTING CART PRODUCT "+filteredCartProduct.get());
                filteredCartProduct.get().setProductId(cartProd.getProductId());
                filteredCartProduct.get().setProductName(cartProd.getProductName());
                filteredCartProduct.get().setPrice(filteredCartProduct.get().getPrice() + cartProd.getPrice());
                filteredCartProduct.get().setQuantity(filteredCartProduct.get().getQuantity() + cartProd.getQuantity());
                LOGGER.info("FILTERED CART PRODUCT"+filteredCartProduct.get());
                existingCustomerCart.get().getCartProducts().add(filteredCartProduct.get());
            }
            existingCustomerCart.get().setTotalPrice(existingCustomerCart.get().getTotalPrice()+(cartProd.getPrice()*cartProd.getQuantity()));
            cartService.createOrSaveCart(existingCustomerCart.get());
            LOGGER.info("CART UPDATED WITH PRODUCT "+cartProd);
            return new ResponseEntity(existingCustomerCart,HttpStatus.OK);
        }else{
            throw new CartServiceException("YOU AREN'T REGISTERED WITH US ! AND CART IS EMPTY");
        }
    }

    @DeleteMapping("/customer/{customerIdForCart}/deleteProduct/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable("customerIdForCart") int customerIdForCart,@PathVariable("productId") int productId) throws CartServiceException{
        String deletedResult = cartService.deleteProductsFromCart(customerIdForCart, productId);

        if(deletedResult.equals("SUCCESS")){
            LOGGER.info("PRODUCT ID ["+productId+"] DELETED FOR CUSTOMER ID ["+customerIdForCart+"]");
            return new ResponseEntity("PRODUCT ID ["+productId+"] DELETED FOR CUSTOMER ID ["+customerIdForCart+"]",HttpStatus.OK);
        }else{
            throw new CartServiceException("ERROR DELETING PRODUCT ID ["+productId+"] AS IT DOESN'T EXIST IN CART");
        }
    }
    public ResponseEntity<?> cartFallback(Exception e){
        return new ResponseEntity("ERROR, WE ARE UNABLE TO COMMUNICATE WITH ORDER SERVICE",HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
