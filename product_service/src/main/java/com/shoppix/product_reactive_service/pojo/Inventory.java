package com.shoppix.product_reactive_service.pojo;

import com.shoppix.product_reactive_service.entity.ProductVariations;
import com.shoppix.product_reactive_service.entity.SKU;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

	private int inventoryId;

	private String inventoryCode;

	private String productId;

	private String productName;

	private List<ProductVariations> productVariants;

	private String category;

	private String subCategory;

	private String productFulfillmentChannel;
	
	private int availableQuantity;

	private int reservedQuantity;

	private int reorderLevel;

	private int reorderQuantity;

	private int stockAlertLevel;
	
	private String stockStatus;

	private String warrantyStatus;

	private String stockType;

	private int damagedQuantity;

	private int returnedQuantity;

	private int minOrderQuantity;

	private List<String> stockHistoryWithDate;

	private String productSeller;

	private String productSupplier;

	private String stockAvailableLocation;

	private String eventStatus;

	private String firstCreatedAt;

	private String lastUpdatedAt;
}
