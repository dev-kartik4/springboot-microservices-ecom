package com.meru.cart_service.service;

import com.meru.cart_service.exception.CartServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meru.cart_service.entity.Cart;
import com.meru.cart_service.repo.CartRepo;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CartService {
	
	@Autowired
	public CartRepo cartRepo;

	private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);

	public Cart createOrSaveCart(Cart customerCart){

		Optional<Cart> existingCart = getCartDetails(customerCart.getCustomerIdForCart());
		if(existingCart.isEmpty()){
			Cart custCart = new Cart();
			custCart.setCustomerIdForCart(customerCart.getCustomerIdForCart());
			custCart.setCartProducts(customerCart.getCartProducts());
			custCart.setTotalPrice(customerCart.getTotalPrice());
			LOGGER.info("CART CREATED FOR CUSTOMER WITH ID ["+custCart.getCustomerIdForCart()+"]");
			return cartRepo.save(custCart);
		}else{
			Cart custCart = getCartDetails(customerCart.getCustomerIdForCart()).get();
			custCart.setCustomerIdForCart(customerCart.getCustomerIdForCart());
			custCart.setCartProducts(customerCart.getCartProducts());
			custCart.setTotalPrice(customerCart.getTotalPrice());
			LOGGER.info("CART UPDATED WITH YOUR PRODUCTS FOR CUSTOMER WITH ID ["+custCart.getCustomerIdForCart()+"]");
			return cartRepo.save(custCart);
		}
	}

	public Optional<Cart> getCartDetails(int customerIdForCart) throws CartServiceException {

		Optional<Cart> cartData = cartRepo.findByCustomerIdForCart(customerIdForCart);

		if(cartData.isPresent()){
			LOGGER.info("FETCHED CART DETAILS FOR CUSTOMER ID ["+customerIdForCart+"]");
			return cartData;
		}else{
			throw new CartServiceException("ERROR FETCHING CART DETAILS FOR CUSTOMER ID ["+customerIdForCart+"]");
		}
	}

	public String deleteProductsFromCart(int customerIdForCart,int productId) throws CartServiceException{
		Optional<Cart> customerCart = getCartDetails(customerIdForCart);

		AtomicReference<String> cartProductDeleted = new AtomicReference<>("SUCCESS");
		customerCart.get().getCartProducts().forEach(cartProduct -> {
			if(cartProduct.getProductId() == productId){
				customerCart.get().getCartProducts().remove(cartProduct);
				customerCart.get().setTotalPrice(customerCart.get().getTotalPrice()-cartProduct.getPrice());
				customerCart.get().setCustomerIdForCart(customerCart.get().getCustomerIdForCart());
				createOrSaveCart(customerCart.get());
			}else{
				cartProductDeleted.set("FAILED");
				throw new CartServiceException("ERROR DELETING CART PRODUCT FOR CUSTOMER ID ["+customerIdForCart+"]");
			}
		});
		return cartProductDeleted.get();
	}

}
