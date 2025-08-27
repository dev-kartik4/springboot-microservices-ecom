package com.shoppix.product_reactive_service.advice;

import com.shoppix.product_reactive_service.exception.ProductServiceException;
import com.shoppix.product_reactive_service.pojo.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProductServiceExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<ResponseMessage> handleProductServiceException(ProductServiceException productEx){

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseMessage.setMessage(productEx.getMessage());
        productEx.printStackTrace();
        return new ResponseEntity(responseMessage, HttpStatus.valueOf(responseMessage.getStatusCode()));
    }
}
