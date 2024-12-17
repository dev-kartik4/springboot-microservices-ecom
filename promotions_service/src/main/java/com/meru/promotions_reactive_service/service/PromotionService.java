package com.meru.promotions_reactive_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meru.promotions_reactive_service.entity.Promotions;
import com.meru.promotions_reactive_service.repo.PromotionRepo;

@Service
public class PromotionService {
	
	@Autowired
	public PromotionRepo promoRepo;
	
	
	public Promotions savePromo(Promotions promo) {
		
		return promoRepo.save(promo);
	}
	
	
	public List<Promotions> getAllPromotions(){
		
		List<Promotions> promos = new ArrayList<Promotions>();
		promoRepo.findAll().forEach(promos::add);
		
		return promos;
		
	}
	
	public Optional<Promotions> getPromoById(int promotionId) {
		
		return promoRepo.findById(promotionId);
	}
	
	public Promotions updatePromo(Promotions promo) {
		
		return promoRepo.save(promo);
	}
	
	public void deleteByPromoId(int promotionId) {
		
		promoRepo.deleteById(promotionId);
		
	}


//	public List<Promotions> getAllPromosByProductId(String productId) {
//
//		List<Promotions> promos = new ArrayList<Promotions>();
//		promoRepo.findAllByProductId(productId).forEach(promos::add);
//
//		return promos;
//	}
}
