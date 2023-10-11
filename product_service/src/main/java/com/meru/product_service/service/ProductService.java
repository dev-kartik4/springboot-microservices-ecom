package com.meru.product_service.service;

import java.util.ArrayList;
import java.util.List;

import com.meru.product_service.exception.ProductServiceException;
import com.meru.product_service.pojo.Inventory;
import com.meru.product_service.utility.ProductIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	public ProductIdGenerator productIdGenerator;

	@Autowired
	public RestTemplate restTemplate;

	@Autowired
	public SequenceGeneratorService sequenceGeneratorService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

	public Product saveProduct(Product prod) throws ProductServiceException{

		Product prod1 = productRepo.findProductByProductName(prod.getProductName());

		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+prod.getProductId(), Inventory.class);
		String pattern="PROD#"+prod.getCategory().substring(0,3)+"-";
		String productKey = productIdGenerator.generateProductId(prod);
		int pid = (int) sequenceGeneratorService.generateSequence(Product.SEQUENCE_NAME);
		if(inv==null && prod1 == null) {
			Inventory inv1=new Inventory();
			inv1.setProductId(pid);
			inv1.setQuantity(1);
			inv1.setStatus("AVAILABLE IN STOCK");
			restTemplate.postForObject("http://inventory-service/inventory/addInventory", inv1, Inventory.class);
			prod.setProductId(pid);
			prod.setProductSerialNumber(pattern+productKey+"-#"+pid);
			prod.setStockStatus(inv1.getStatus());
			productRepo.save(prod);
			LOGGER.info("NEW PRODUCT ADDED AND UPDATED SUCCESSFULLY");
			return prod1;
		}else{
			LOGGER.error("ERROR WHILE SAVING PRODUCT ! PRODUCT ID ["+prod.getProductId()+ "] ALREADY EXISTS");
			throw new ProductServiceException("ERROR WHILE SAVING PRODUCT ! PRODUCT ID ["+prod.getProductId()+ "] ALREADY EXISTS");
		}
	}
	
	public List<Product> getAllProducts() throws ProductServiceException{
		
		List<Product> products = new ArrayList<Product>();
		productRepo.findAll().forEach(products::add);
		if(!products.isEmpty()){
			LOGGER.info("FETCHING ALL THE PRODUCTS SUCCESSFULLY");
			return products;
		}else{
			LOGGER.error("ERROR WHILE COMMUNICATING SERVICE AND FETCHING ALL PRODUCTS");
			throw new ProductServiceException("ERROR WHILE COMMUNICATING SERVICE AND FETCHING ALL PRODUCTS");
		}


	}

	public Product updateProduct(Product prod,int productId) throws ProductServiceException{
		Product existingProduct = productRepo.findById(productId).get();
		if(existingProduct != null){
			existingProduct.setProductId(productId);
			existingProduct.setProductName(prod.getProductName());
			existingProduct.setPrice(prod.getPrice());
			existingProduct.setModelNumber(prod.getModelNumber());
			existingProduct.setDimensions(prod.getDimensions());
			existingProduct.setCategory(prod.getCategory());
			existingProduct.setStockStatus(prod.getStockStatus());
			existingProduct.setPromosAvailable(prod.getPromosAvailable());

			LOGGER.info("PRODUCT UPDATED SUCCESSFULLY");
			return productRepo.save(existingProduct);
		}else{
			LOGGER.error("ERROR WHILE FETCHING PRODUCT WITH PRODUCT ID ["+productId+"]");
			throw new ProductServiceException("ERROR WHILE FETCHING PRODUCT WITH PRODUCT ID ["+productId+"]");
		}

	}

	public Product getProductById(int productId) throws ProductServiceException{
		Product product = productRepo.findById(productId).get();
		if(product != null){
			LOGGER.info("PRODUCT DETAILS FETCHED SUCCESSFULLY");
			return product;
		}else{
			LOGGER.error("ERROR WHILE FETCHING PRODUCT WITH PRODUCT ID ["+productId+"]");
			throw new ProductServiceException("ERROR WHILE FETCHING PRODUCT WITH PRODUCT ID ["+productId+"]");
		}
	}
	
	public String deleteProductById(int productId) {

		boolean productDeleted = false;
		String result = "";
		Inventory inv = restTemplate.getForObject("http://inventory-service/inventory/getInventory/"+productId,Inventory.class);
		if(!productDeleted){
			productRepo.deleteByProductId(productId);
			restTemplate.delete("http://inventory-service/inventory/delete/"+inv.getInventoryId());
			LOGGER.info("PRODUCT AND INVENTORY DETAILS DELETED WITH"+productId);
			result = "SUCCESS";
		}else{
			result = "FAILURE";
			LOGGER.error("ERROR WHILE DELETING PRODUCT WITH PRODUCT ID ["+productId+"] | NOT FOUND");
			throw new ProductServiceException("ERROR WHILE DELETING PRODUCT WITH PRODUCT ID ["+productId+"] | NOT FOUND");
		}
		return result;
	}


	public Product getProductByName(String productName) {
		Product prod = productRepo.findProductByProductName(productName);
		if(prod != null){
			LOGGER.info("FETCHED PRODUCT DETAILS WITH NAME SUCCESSFULLY");
			return prod;
		}else{
			LOGGER.error("ERROR WHILE FETCHING PRODUCT WITH PRODUCT NAME ["+productName+"]");
			throw new ProductServiceException("ERROR WHILE FETCHING PRODUCT WITH PRODUCT NAME ["+productName+"]");
		}
	}
}
