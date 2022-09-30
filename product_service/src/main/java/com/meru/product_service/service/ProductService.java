package com.meru.product_service.service;

import java.util.ArrayList;
import java.util.List;

import com.meru.product_service.pojo.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meru.product_service.entity.Product;
import com.meru.product_service.repo.ProductRepo;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductService{

	@Autowired
	public ProductRepo productRepo;

	@Autowired
	public RestTemplate restTemplate;

	public Product saveProduct(Product prod){

		Product prod1 = productRepo.findById(prod.getProductId());

		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+prod.getProductId(), Inventory.class);
		if(inv==null) {
			Inventory inv1=new Inventory();
			inv1.setProductId(prod.getProductId());
			inv1.setQuantity(1);
			inv1.setStatus("AVAILABLE IN STOCK");
			restTemplate.postForObject("http://inventory-service/inventory/addInventory", inv1, Inventory.class);
			prod.setStockStatus(inv1.getStatus());
			productRepo.save(prod);
		}
//		else {
//			if(inv.getQuantity()==0)
//				inv.setStatus("AVAILABLE IN STOCK");
//			inv.setQuantity(inv.getQuantity()+1);
//			restTemplate.postForObject("http://inventory-service/inventory/updateInventory/"+inv.getInventoryId(), inv, Inventory.class);
//			System.out.println(inv);
//		}
		return prod1;
	}
	
	public List<Product> getAllProducts(){
		
		List<Product> products = new ArrayList<Product>();
		productRepo.findAll().forEach(products::add);
		
		return products;
	}

	public String updateProduct(Product prod,int productId){
		Product prod1 = productRepo.findById(productId);
		prod1.setProductId(productId);
		prod1.setProductName(prod.getProductName());
		prod1.setPrice(prod.getPrice());
		prod1.setModelNumber(prod.getModelNumber());
		prod1.setDimensions(prod.getDimensions());
		prod1.setCategory(prod.getCategory());
		prod1.setStockStatus(prod.getStockStatus());
		prod1.setPromosAvailable(prod.getPromosAvailable());

		productRepo.save(prod1);

		return "PRODUCT UPDATED SUCCESSFULLY";
	}
	
	public Product getProductById(int productId) {
		
		return productRepo.findById(productId);
	}
	
	public String deleteProductById(int productId) {

		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+productId,Inventory.class);
		restTemplate.delete("http://inventory-service/inventory/delete/"+inv.getInventoryId());
		productRepo.deleteById(productId);
		return "PRODUCT AND INVENTORY DETAILS DELETED WITH"+productId;
	}

	
}
