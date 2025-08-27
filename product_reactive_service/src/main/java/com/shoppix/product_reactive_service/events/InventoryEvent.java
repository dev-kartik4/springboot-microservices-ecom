package com.shoppix.product_reactive_service.events;

import com.shoppix.product_reactive_service.pojo.Inventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryEvent {

    private int inventoryId;

    private long productId;

    private String inventoryMessageType;

    private Inventory inventory;

}
