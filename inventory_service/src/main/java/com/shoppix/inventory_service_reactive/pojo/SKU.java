package com.shoppix.inventory_service_reactive.pojo;

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
