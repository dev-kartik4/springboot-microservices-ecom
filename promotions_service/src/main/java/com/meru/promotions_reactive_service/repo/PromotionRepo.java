package com.meru.promotions_reactive_service.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.meru.promotions_reactive_service.entity.Promotions;

@Repository
public interface PromotionRepo extends MongoRepository<Promotions, Integer> {

}
