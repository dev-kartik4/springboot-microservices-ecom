package com.shoppix.product_reactive_service.pojo;

import com.shoppix.product_reactive_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProduct {
	
	private Product prod;
	
	private Inventory inv;

	private Offers offers;
}
