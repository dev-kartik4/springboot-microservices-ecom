package com.meru.product_service.pojo;

import com.meru.product_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProduct {
	
	private Product prod;
	
	private Inventory inv;
}
