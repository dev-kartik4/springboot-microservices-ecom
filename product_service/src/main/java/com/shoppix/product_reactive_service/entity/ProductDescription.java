package com.shoppix.product_reactive_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDescription {

    @Field(name = "COLOR")
    private String color;

    @Field(name = "SIZE")
    private String size;

    @Field(name = "STYLE_NAME")
    private String styleName;

    @Field(name = "PATTERN_NAME")
    private String patternName;

    @Field(name = "CAPACITY")
    private String capacity;

    @Field(name = "DIMENSIONS")
    private String dimensions;
}
