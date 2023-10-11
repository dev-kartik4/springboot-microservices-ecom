package com.meru.product_service.advice;

import com.meru.product_service.exception.ProductServiceException;
import com.meru.product_service.pojo.ResponseErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProductServiceExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<ResponseErrorMessage> handleCustomerServiceException(ProductServiceException productEx){

        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage();
        responseErrorMessage.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseErrorMessage.setMessage(productEx.getMessage());
        productEx.printStackTrace();
        return new ResponseEntity(responseErrorMessage, HttpStatus.valueOf(responseErrorMessage.getStatusCode()));
    }
}
