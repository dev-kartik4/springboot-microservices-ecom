package com.meru.product_service.pojo;

import com.meru.product_service.entity.Product;

import java.util.List;

public class CompleteProduct {
	
	private Product prod;
	
	private Inventory inv;
	
	private List<Promotions> promos;

	public CompleteProduct() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CompleteProduct(Product prod, Inventory inv, List<Promotions> promos) {
		super();
		this.prod = prod;
		this.inv = inv;
		this.promos = promos;
	}

	public Product getProd() {
		return prod;
	}

	public void setProd(Product prod) {
		this.prod = prod;
	}

	public Inventory getInv() {
		return inv;
	}

	public void setInv(Inventory inv) {
		this.inv = inv;
	}

	public List<Promotions> getPromos() {
		return promos;
	}

	public void setPromo(List<Promotions> promos) {
		this.promos = promos;
	}

	@Override
	public String toString() {
		return "CompleteProduct [prod=" + prod + ", inv=" + inv + ", promos=" + promos + "]";
	}
	
}
