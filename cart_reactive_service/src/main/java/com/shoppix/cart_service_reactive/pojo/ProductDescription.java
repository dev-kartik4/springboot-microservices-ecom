package com.shoppix.cart_service_reactive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

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
