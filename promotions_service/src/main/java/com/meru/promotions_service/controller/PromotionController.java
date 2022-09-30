package com.meru.promotions_service.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.meru.promotions_service.entity.Promotions;
import com.meru.promotions_service.service.PromotionService;

@RestController
@RequestMapping("/promotions")
public class PromotionController {
	
	
	@Autowired
	public PromotionService promoService;
	
	@PostMapping("/addPromo")
	@ResponseBody
	public Promotions savePromo(@RequestBody Promotions promo) {
		
		return promoService.savePromo(promo);
	}
	
	@GetMapping("/getPromo/{promotionId}")
	public Optional<Promotions> getPromoById(@PathVariable("promotionId") int promotionId){
		
		return promoService.getPromoById(promotionId);
	}
	
//	@GetMapping("/getPromoByProduct/{productId}")
//	public List<Promotions> getAllPromosByProductId(@PathVariable("productId") String productId) {
//
//		return promoService.getAllPromosByProductId(productId);
//	}
	
	@PutMapping("/updatePromo")
	@ResponseBody
	public Promotions updatePromo(@RequestBody Promotions promo) {
		
		return promoService.updatePromo(promo);
	}
	
	@DeleteMapping("/delete/{promotionId}")
	public void deleteByPromoId(@PathVariable("promotionId") int promotionId) {
		
		promoService.deleteByPromoId(promotionId);
	}

}
