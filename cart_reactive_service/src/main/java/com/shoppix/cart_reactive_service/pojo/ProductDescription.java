package com.shoppix.cart_reactive_service.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDescription {

    private String color;

    private String size;

    private String styleName;

    private String patternName;

    private String capacity;

    private String dimensions;
}
