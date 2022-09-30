package com.meru.product_service.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.meru.product_service.entity.Product;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface ProductRepo extends MongoRepository<Product, Integer> {

    Product findById(int productId);

    void deleteById(int productId);
}
