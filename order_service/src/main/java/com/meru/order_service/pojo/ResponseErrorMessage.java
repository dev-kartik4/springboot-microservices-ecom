package com.meru.order_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseErrorMessage {

    private int statusCode;

    private String message;
}
