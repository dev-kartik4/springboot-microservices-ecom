package com.shoppix.customer_service_reactive.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDescription {

    private String color;

    private String size;

    private String styleName;

    private String patternName;

    private String capacity;

    private String dimensions;
}
