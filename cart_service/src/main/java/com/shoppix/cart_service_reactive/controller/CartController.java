package com.shoppix.cart_service_reactive.controller;

import com.shoppix.cart_service_reactive.entity.Cart;
import com.shoppix.cart_service_reactive.exception.CartServiceException;
import com.shoppix.cart_service_reactive.repo.CartRepo;
import com.shoppix.cart_service_reactive.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@CrossOrigin("*")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    public CartService cartService;

    @Autowired
    public WebClient.Builder webClientBuilder;

    private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);

    private static final String CART_SERVICE = "cartService";
    @Autowired
    private CartRepo cartRepo;

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO CREATE OR SAVE NEW CART FOR FIRST TIME
     *
     * @param cart
     * @return
     * @throws CartServiceException
     */
    @PostMapping("/createCart/customer")
    @ResponseBody
    public ResponseEntity<Mono<Cart>> createCart(@RequestBody Cart cart) throws CartServiceException{

        Mono<Cart> customerCart = cartService.createOrUpdateCart(cart);
        return new ResponseEntity(customerCart,HttpStatus.OK);
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO FETCH CART DETAILS BY CUSTOMER ID FOR CART
     *
     * @param customerIdForCart
     * @return
     * @throws CartServiceException
     */
    @GetMapping("/viewCart/customer/{customerIdForCart}")
    public ResponseEntity<Mono<Cart>> getCartDetails(@PathVariable("customerIdForCart") int customerIdForCart) throws CartServiceException{

        LOGGER.info("LOADING YOUR CART DETAILS.....");
        Mono<Cart> cartDetails = cartService.getCartDetails(customerIdForCart);

        return new ResponseEntity(cartDetails,HttpStatus.OK);
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO ADD PRODUCT TO CART
     *
     * @param customerIdForCart
     * @param cartProd
     * @return
     * @throws CartServiceException
     */
//    @PutMapping("/customer/{customerIdForCart}/addProductToCart")
//    public ResponseEntity<Mono<Cart>> addProductToCart(@PathVariable("customerIdForCart") int customerIdForCart,@RequestBody CartProduct cartProd) throws CartServiceException{
//
//        Mono<Cart> updatedCartWithProducts = cartService.addProductToCart(customerIdForCart,cartProd);
//
//        return new ResponseEntity(updatedCartWithProducts, HttpStatus.OK);
//    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO DELETE PRODUCT FROM CART
     *
     * @param customerIdForCart
     * @param
     * @return
     * @throws CartServiceException
     */
//    @DeleteMapping("/customer/{customerIdForCart}/deleteProductFromCart/{productId}")
//    public ResponseEntity<Mono<AtomicReference<String>>> deleteProductFromCart(@PathVariable("customerIdForCart") int customerIdForCart,@PathVariable("productId") int productId) throws CartServiceException{
//        Mono<AtomicReference<String>> deletedResult = cartService.deleteProductsFromCart(customerIdForCart, productId);
//
//        if(deletedResult.equals("SUCCESS")){
//            LOGGER.info("PRODUCT ID ["+productId+"] DELETED FOR CUSTOMER ID ["+customerIdForCart+"]");
//            return new ResponseEntity("PRODUCT ID ["+productId+"] DELETED FOR CUSTOMER ID ["+customerIdForCart+"]",HttpStatus.OK);
//        }else{
//            LOGGER.error("ERROR DELETING PRODUCT ID ["+productId+"] AS IT DOESN'T EXIST IN CART");
//            throw new CartServiceException("ERROR DELETING PRODUCT ID ["+productId+"] AS IT DOESN'T EXIST IN CART");
//        }
//    }

    @DeleteMapping("/customer/{customerIdForCart}/deleteCartForCustomer")
    public Mono<ResponseEntity<String>> deleteCartData(@PathVariable("customerIdForCart") int customerIdForCart) {
        LOGGER.info("FETCHING.. INFO TO DELETE CART WITH CUSTOMER ID [" + customerIdForCart + "]");
        return cartRepo.deleteById(customerIdForCart)
                .then(Mono.just(new ResponseEntity<>(
                        "CART SUCCESSFULLY DELETED FOR CUSTOMER ID [" + customerIdForCart + "]",
                        HttpStatus.ACCEPTED)))
                .onErrorResume(e -> {
                    LOGGER.error("Error deleting cart with ID [{}]", customerIdForCart, e);
                    return Mono.error(new CartServiceException("FAILED TO DELETE CART WITH CUSTOMER ID ["+customerIdForCart+"]"));
                });
    }
}
