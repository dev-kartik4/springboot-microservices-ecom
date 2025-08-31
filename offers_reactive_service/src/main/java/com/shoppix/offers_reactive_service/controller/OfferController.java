package com.shoppix.offers_reactive_service.controller;

import java.util.Optional;

import com.shoppix.offers_reactive_service.entity.Offers;
import com.shoppix.offers_reactive_service.service.OffersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promotions")
public class OfferController {

    @Autowired
    private OffersService offersService;

//	@PostMapping("/addOffer")
//	@ResponseBody
//	public Offers saveOffers(@RequestBody Offers offers) {
//
//		return offersService.saveOffers(offers);
//	}
//
//	@GetMapping("/getOffer/{offerId}")
//	public Optional<Offers> getOfferById(@PathVariable("offerId") int offerId){
//
//		return offersService.getPromoById(offerId);
//	}
//
////	@GetMapping("/getPromoByProduct/{productId}")
////	public List<Promotions> getAllPromosByProductId(@PathVariable("productId") String productId) {
////
////		return promoService.getAllPromosByProductId(productId);
////	}
//
//	@PutMapping("/updateOffer")
//	@ResponseBody
//	public Offers updatePromo(@RequestBody Offers offer) {
//
//		return offersService.updateOffer(offer);
//	}
//
//	@DeleteMapping("/delete/{offerId}")
//	public void deleteByPromoId(@PathVariable("offerId") int offerId) {
//
//		promoService.deleteByOfferId(offerId);
//	}

}
