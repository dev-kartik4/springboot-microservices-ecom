package com.meru.cart_service.repo;

//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.meru.cart_service.entity.Cart;

import java.util.Optional;

@Repository
public interface CartRepo extends MongoRepository<Cart, Integer> {

    Optional<Cart> findByCustomerIdForCart(int customerIdForCart);
}
