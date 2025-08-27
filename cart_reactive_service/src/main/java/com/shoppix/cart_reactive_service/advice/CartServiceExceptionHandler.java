package com.shoppix.cart_reactive_service.advice;

import com.shoppix.cart_reactive_service.exception.CartServiceException;
import com.shoppix.cart_reactive_service.pojo.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CartServiceExceptionHandler {
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CartServiceException.class)
    public ResponseEntity<ResponseMessage> handleCustomerServiceException(CartServiceException cartEx){

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseMessage.setMessage(cartEx.getMessage());
        cartEx.printStackTrace();
        return new ResponseEntity(responseMessage, HttpStatus.valueOf(responseMessage.getStatusCode()));
    }
}
