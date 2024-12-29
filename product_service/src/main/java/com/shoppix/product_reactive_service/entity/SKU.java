package com.shoppix.product_reactive_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SKU{

    @Id
    @Field(name = "SKU_CODE")
    private String skuCode;

    @Field(name = "QUANTITY_IN_STOCK")
    private int quantityOfStock;
}
