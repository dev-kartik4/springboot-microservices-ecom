package com.meru.inventory_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.meru.inventory_service.entity.Inventory;
import com.meru.inventory_service.repo.InventoryRepo;

@Service
public class InventoryService {
	
	@Autowired
	public InventoryRepo invRepo;

	@Autowired
	public MongoTemplate mongoTemplate;

	public Inventory saveInv(Inventory inv) {

		//inv.setInventoryId(sequenceGeneratorService.generateSequence(Inventory.SEQUENCE_NAME));
		if(getAllInventory().size() == 0){
			inv.setInventoryId(1);
			return invRepo.save(inv);
		}else{
			inv.setInventoryId(getAllInventory().size()+1);
			return invRepo.save(inv);
		}
	}
	
	public Inventory getInvById(int inventoryId) {
		
		return invRepo.findById(inventoryId).get();
	}
	public List<Inventory> getAllInventory(){
		List<Inventory> inventoryList = new ArrayList<>();
		invRepo.findAll().forEach(inventoryList::add);
		return inventoryList;
	}
	public Inventory updateInventory(int inventoryId,Inventory inv) {
		Optional<Inventory> inv1 = invRepo.findById(inventoryId);
		System.out.println("UPDATING INVENTORY");
		if(inv1.isPresent()){
			deleteByInvId(inv1.get().getInventoryId());
			Inventory inventory = inv1.get();
			inventory.setQuantity(inv.getQuantity());
			inventory.setStatus(inv.getStatus());
			inventory.setProductId(inv.getProductId());
			return invRepo.save(inventory);
		}
		return invRepo.save(inv);
	}
	
	public void deleteByInvId(int inventoryId) {
		
		invRepo.deleteById(inventoryId);
	}

	public Inventory getInvByProductId(int productId) {
		List<Inventory> inv=invRepo.findAll();
		Inventory inv1 = null;
		for(Inventory temp:inv) {
			if(temp.getProductId()==productId)
				return temp;
		}
		return inv1;
	}
	
}
