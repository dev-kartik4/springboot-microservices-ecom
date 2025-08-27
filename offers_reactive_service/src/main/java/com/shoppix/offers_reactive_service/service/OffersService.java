package com.shoppix.offers_reactive_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.shoppix.offers_reactive_service.entity.Offers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shoppix.offers_reactive_service.repo.OffersRepo;
import reactor.core.publisher.Mono;

@Service
public class OffersService {

    @Autowired
    private OffersRepo offersRepo;


	public Mono<Offers> saveOffers(Offers offers) {
		
		return offersRepo.save(offers);
	}
	
	
	public List<Offers> getAllOffers(){
		
		List<Offers> offers = new ArrayList<Offers>();
		offersRepo.findAll().forEach(offers::add);
		
		return offers;
		
	}
	
	public Optional<Offers> getPromoById(int offerId) {
		
		return offersRepo.findById(offer);
	}
	
	public Offers updateOffer(Offers offer) {
		
		return offersRepo.save(offer);
	}
	
	public void deleteByOfferId(int offerId) {
		
		offersRepo.deleteById(offerId);
		
	}


//	public List<Promotions> getAllPromosByProductId(String productId) {
//
//		List<Promotions> promos = new ArrayList<Promotions>();
//		promoRepo.findAllByProductId(productId).forEach(promos::add);
//
//		return promos;
//	}
}
