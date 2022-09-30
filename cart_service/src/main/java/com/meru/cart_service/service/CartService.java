package com.meru.cart_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meru.cart_service.entity.Cart;
import com.meru.cart_service.repo.CartRepo;

import java.util.Optional;

@Service
public class CartService {
	
	@Autowired
	public CartRepo cartRepo;

}
