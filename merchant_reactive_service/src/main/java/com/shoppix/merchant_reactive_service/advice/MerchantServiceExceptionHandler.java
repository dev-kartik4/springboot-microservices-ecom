package com.shoppix.merchant_reactive_service.advice;

import com.shoppix.merchant_reactive_service.exception.MerchantServiceException;
import com.shoppix.merchant_reactive_service.pojo.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MerchantServiceExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MerchantServiceException.class)
    public ResponseEntity<ResponseMessage> handleMerchantServiceException(MerchantServiceException merchantEx){

        ResponseMessage responseErrorMessage = new ResponseMessage();
        responseErrorMessage.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseErrorMessage.setMessage(merchantEx.getMessage());
        merchantEx.printStackTrace();
        return new ResponseEntity<>(responseErrorMessage, HttpStatus.valueOf(responseErrorMessage.getStatusCode()));
    }
}
