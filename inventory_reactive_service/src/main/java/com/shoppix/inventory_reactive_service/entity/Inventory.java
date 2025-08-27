package com.shoppix.inventory_reactive_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shoppix.inventory_reactive_service.pojo.ProductVariations;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "INVENTORY_DETAILS")
public class Inventory {

	@Id
	@Field(name = "INVENTORY_ID")
	private long inventoryId;

	@Field(name = "INVENTORY_CODE")
	private String inventoryCode;

	@Field(name = "PRODUCT_ID")
	private String parentProductId;

	@Field(name = "PRODUCT_NAME")
	private String productName;

	@Field(name = "MERCHANT_ID")
	private long merchantId;

	@Field(name = "MERCHANT_SELLING_NAME")
	private String merchantSellingName;

	@Field(name = "ALL_PRODUCT_VARIANTS")
	private List<ProductVariations> productVariants;

	@Field(name = "CATEGORY")
	private String category;

	@Field(name = "SUB_CATEGORY")
	private String subCategory;

	@Field(name = "PRODUCT_FULFILLED_BY")
	private String productFulfillmentChannel;

	@Field(name = "AVAILABLE_QUANTITY")
	private int availableQuantity;

	@Field(name = "RESERVED_QUANTITY")
	private int reservedQuantity;

	@Field(name = "REORDER_LEVEL")
	private int reorderLevel;

	@Field(name = "REORDER_QUANTITY")
	private int reorderQuantity;

	@Field(name = "STOCK_ALERT_LEVEL")
	private int stockAlertLevel;

	@Field(name = "PRODUCT_STOCK_STATUS")
	private String stockStatus;

	@Field(name = "PRODUCT_WARRANTY_STATUS")
	private String warrantyStatus;

	@Field(name = "INVENTORY_STOCK_TYPE")
	private String stockType;

	@Field(name = "DAMAGED_QUANTITY")
	private int damagedQuantity;

	@Field(name = "RETURNED_QUANTITY")
	private int returnedQuantity;

	@Field(name = "MINIMUM_ORDER_QUANTITY")
	private int minOrderQuantity;

	@Field(name = "STOCK_HISTORY_WITH_DATE")
	private List<String> stockHistoryWithDate;

	@Field(name = "PRODUCT_SELLER")
	private String productSeller;

	@Field(name = "PRODUCT_SUPPLIER")
	private String productSupplier;

	@Field(name = "EVENT_STATUS")
	@JsonIgnore
	private String eventStatus;

	@Field(name = "FIRST_CREATED_AT")
	@JsonIgnore
	private String firstCreatedAt;

	@Field(name = "LAST_UPDATED_AT")
	@JsonIgnore
	private String lastUpdatedAt;
}
