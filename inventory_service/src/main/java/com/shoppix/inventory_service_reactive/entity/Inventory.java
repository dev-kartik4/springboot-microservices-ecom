package com.shoppix.inventory_service_reactive.entity;

import com.shoppix.inventory_service_reactive.pojo.SKU;
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
	private int inventoryId;

	@Field(name = "INVENTORY_CODE")
	private String inventoryCode;

	@Field(name = "SKU_OF_PRODUCT_IN_INVENTORY")
	private List<SKU> skuList;

	@Field(name = "PRODUCT_ID")
	private long productId;

	@Field(name = "PRODUCT_NAME")
	private String productName;

	@Field(name = "PRODUCT_BRAND")
	private String productBrand;

	@Field(name = "PRODUCT_PRICE")
	private double productPrice;

	@Field(name = "MODEL_NUMBER")
	private String modelNumber;

	@Field(name = "PRODUCT_SERIAL_NUMBER")
	private String productSerialNumber;

	@Field(name = "CATEGORY")
	private String category;

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

	@Field(name = "STOCK_AVAILABLE_LOCATION")
	private String stockAvailableLocation;

	@Field(name = "EVENT_STATUS")
	private String eventStatus;

	@Field(name = "FIRST_CREATED_AT")
	private String firstCreatedAt;

	@Field(name = "LAST_UPDATED_AT")
	private String lastUpdatedAt;
}
