package com.meru.inventory_service.controller;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.meru.inventory_service.entity.Inventory;
import com.meru.inventory_service.service.InventoryService;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
	
	@Autowired
	public InventoryService invService;
	
	@PostMapping("/addInventory")
	@ResponseBody
	public Inventory saveInv(@RequestBody Inventory inv) {
		
		return invService.saveInv(inv);
	}
	
	@GetMapping("/getInv/{inventoryId}")
	public Inventory getByInventoryId(@PathVariable("inventoryId") int inventoryId) {
		
		return invService.getInvById(inventoryId);
	}
	
	@GetMapping("/getInventory/{productId}")
	public Inventory getInventoryByProductId(@PathVariable("productId") int productId) {
		
		return invService.getInvByProductId(productId);
	}
	
	
	@PutMapping("/updateInventory/{inventoryId}")
	@ResponseBody
	public Inventory updateInv(@PathVariable("inventoryId") int inventoryId,@RequestBody Inventory inv) {
		
		return invService.updateInventory(inventoryId,inv);
	}
	
	
	@DeleteMapping("/delete/{inventoryId}")
	public void deleteById(@PathVariable("inventoryId") int inventoryId) {
		
		invService.deleteByInvId(inventoryId);
	}
	
	

}
