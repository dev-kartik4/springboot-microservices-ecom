package com.shoppix.merchant_service_reactive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseErrorMessage {

    private int statusCode;

    private String message;
}
