package com.shoppix.inventory_service_reactive.events;

import com.shoppix.inventory_service_reactive.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryEvent {

    private int inventoryId;

    private String parentProductId;

    private String inventoryMessageType;

    private Inventory inventory;

}
