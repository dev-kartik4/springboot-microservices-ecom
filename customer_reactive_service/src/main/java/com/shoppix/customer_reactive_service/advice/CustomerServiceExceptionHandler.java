package com.shoppix.customer_reactive_service.advice;

import com.shoppix.customer_reactive_service.exception.CustomerServiceException;
import com.shoppix.customer_reactive_service.model.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
public class CustomerServiceExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CustomerServiceException.class)
    public ResponseEntity<ResponseMessage> handleCustomerServiceException(CustomerServiceException custEx){

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseMessage.setMessage(custEx.getMessage());
        responseMessage.setTimestamp(String.valueOf(new Date()));
        custEx.printStackTrace();
        return new ResponseEntity(responseMessage, HttpStatus.valueOf(responseMessage.getStatusCode()));
    }
}
