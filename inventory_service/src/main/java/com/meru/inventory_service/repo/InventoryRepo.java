package com.meru.inventory_service.repo;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.meru.inventory_service.entity.Inventory;

@Repository
public interface InventoryRepo extends MongoRepository<Inventory, Integer> {

	Inventory findByProductId(String productId);


}
