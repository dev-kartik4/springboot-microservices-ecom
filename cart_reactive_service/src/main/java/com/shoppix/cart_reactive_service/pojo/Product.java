package com.shoppix.cart_reactive_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String parentProductId;

    private String productName;

    private List<ProductVariations> productVariations;

    private String category;

    private String subCategory;

    private String productFulfillmentChannel;

    private String productManufacturer;

    private String productSeller;

    private long merchantId;

    private String merchantSellingName;

    private List<String> availablePincodesForProduct;

    private String eventStatus;

    private String createdDateTime;

    private String lastUpdatedDateTime;

}
