package com.shoppix.inventory_reactive_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SKU {

    private String skuCode;

    private int quantityOfStock;
}
