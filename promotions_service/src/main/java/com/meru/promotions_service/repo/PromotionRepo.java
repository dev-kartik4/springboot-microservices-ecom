package com.meru.promotions_service.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.meru.promotions_service.entity.Promotions;

@Repository
public interface PromotionRepo extends MongoRepository<Promotions, Integer> {

}
