package com.shoppix.product_reactive_service.pojo;

import com.shoppix.product_reactive_service.entity.ProductVariations;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

	private long inventoryId;

	private String inventoryCode;

	private String parentProductId;

	private String productName;

	private long merchantId;

	private String merchantSellingName;

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

	private String eventStatus;

	private String firstCreatedAt;

	private String lastUpdatedAt;
}
