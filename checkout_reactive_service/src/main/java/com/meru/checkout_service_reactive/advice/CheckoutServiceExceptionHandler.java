package com.meru.checkout_service_reactive.advice;

import com.meru.checkout_service_reactive.exception.CheckoutServiceException;
import com.meru.checkout_service_reactive.pojo.ResponseErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CheckoutServiceExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CheckoutServiceException.class)
    public ResponseEntity<ResponseErrorMessage> handleCustomerServiceException(CheckoutServiceException checkEx) {

        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage();
        responseErrorMessage.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseErrorMessage.setMessage(checkEx.getMessage());
        checkEx.printStackTrace();
        return new ResponseEntity(responseErrorMessage, HttpStatus.valueOf(responseErrorMessage.getStatusCode()));
    }
}
