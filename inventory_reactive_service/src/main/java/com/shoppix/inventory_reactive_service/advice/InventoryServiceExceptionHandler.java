package com.shoppix.inventory_reactive_service.advice;

import com.shoppix.inventory_reactive_service.exception.InventoryServiceException;
import com.shoppix.inventory_reactive_service.pojo.ResponseErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InventoryServiceExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(InventoryServiceException.class)
    public ResponseEntity<ResponseErrorMessage> handleCustomerServiceException(InventoryServiceException inventoryEx){

        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage();
        responseErrorMessage.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseErrorMessage.setMessage(inventoryEx.getMessage());
        inventoryEx.printStackTrace();
        return new ResponseEntity<>(responseErrorMessage, HttpStatus.valueOf(responseErrorMessage.getStatusCode()));
    }
}
