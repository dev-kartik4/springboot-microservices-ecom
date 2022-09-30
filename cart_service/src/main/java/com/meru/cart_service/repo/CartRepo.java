package com.meru.cart_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.meru.cart_service.entity.Cart;

@Repository
public interface CartRepo extends JpaRepository<Cart, Integer>{

}
