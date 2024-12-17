package com.shoppix.inventory_service_reactive.repo;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.shoppix.inventory_service_reactive.entity.Inventory;
import reactor.core.publisher.Mono;

@Repository
public interface InventoryRepo extends ReactiveMongoRepository<Inventory, Integer> {

	Mono<Inventory> findByProductId(int productId);


}