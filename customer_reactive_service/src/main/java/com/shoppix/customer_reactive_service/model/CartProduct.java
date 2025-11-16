package com.shoppix.customer_reactive_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartProduct {

    @Id
    private List<String> productOrVariantProductIdList;

    private String productName;

    private List<String> productImagesToShow;

    private String stockStatus;

    private double listedPrice;

    private double discountedPrice;

    private int quantity;

    private double averageRating;
}
