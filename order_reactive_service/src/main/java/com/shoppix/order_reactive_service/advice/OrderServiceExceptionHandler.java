package com.shoppix.order_reactive_service.advice;

import com.shoppix.order_reactive_service.exception.OrderServiceException;
import com.shoppix.order_reactive_service.pojo.ResponseErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderServiceExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(OrderServiceException.class)
    public ResponseEntity<ResponseErrorMessage> handleOrderServiceException(OrderServiceException orderEx){

        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage();
        responseErrorMessage.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseErrorMessage.setMessage(orderEx.getMessage());
        orderEx.printStackTrace();
        return new ResponseEntity(responseErrorMessage, HttpStatus.valueOf(responseErrorMessage.getStatusCode()));
    }
}
