package com.meru.product_service.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.meru.product_service.entity.Product;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProductRepo extends MongoRepository<Product, Integer> {

    Optional<Product> findById(int productId);

    void deleteByProductId(int productId);

    @Query("{productName: ?0}")
    Product findProductByProductName(String productName);

}
