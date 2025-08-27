package com.shoppix.inventory_reactive_service.events;

import com.shoppix.inventory_reactive_service.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryEvent {

    private long inventoryId;

    private String parentProductId;

    private String inventoryMessageType;

    private Inventory inventory;

}
