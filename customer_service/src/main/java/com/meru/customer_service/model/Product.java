package com.meru.customer_service.model;

public class Product {

	private long productId;
	
	private String productName;
	
	private double price;
	
	private String modelNumber;
	
	private String dimensions;
	
	private String category;

	public Product() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Product(long productId, String productName, double price, String modelNumber, String dimensions,
			String category) {
		super();
		this.productId = productId;
		this.productName = productName;
		this.price = price;
		this.modelNumber = modelNumber;
		this.dimensions = dimensions;
		this.category = category;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getModelNumber() {
		return modelNumber;
	}

	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}

	public String getDimensions() {
		return dimensions;
	}

	public void setDimensions(String dimensions) {
		this.dimensions = dimensions;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "Product [productId=" + productId + ", productName=" + productName + ", price=" + price
				+ ", modelNumber=" + modelNumber + ", dimensions=" + dimensions + ", category=" + category + "]";
	}
	
	
}
