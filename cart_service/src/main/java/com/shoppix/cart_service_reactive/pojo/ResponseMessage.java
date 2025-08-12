package com.shoppix.cart_service_reactive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseMessage {

    private int statusCode;

    private String message;

    private String timestamp;

    private Object responseData;

    private List<String> errorDetails;

    private String path;
}
