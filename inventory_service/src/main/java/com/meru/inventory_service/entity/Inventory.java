package com.meru.inventory_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigInteger;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "INVENTORY_DETAILS")
public class Inventory {

	@Id
	@Field(name = "INVENTORY_ID")
	private int inventoryId;
	
	@Field(name = "QUANTITY")
	private int quantity;

	@Field(name = "STATUS")
	private String status;

	@Field(name = "PRODUCT_ID")
	private String productId;
}
