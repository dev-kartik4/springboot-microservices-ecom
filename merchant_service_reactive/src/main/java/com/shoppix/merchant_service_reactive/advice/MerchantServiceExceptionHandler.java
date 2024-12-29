package com.shoppix.merchant_service_reactive.advice;

import com.shoppix.merchant_service_reactive.exception.MerchantServiceException;
import com.shoppix.merchant_service_reactive.pojo.ResponseErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MerchantServiceExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MerchantServiceException.class)
    public ResponseEntity<ResponseErrorMessage> handleMerchantServiceException(MerchantServiceException merchantEx){

        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage();
        responseErrorMessage.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseErrorMessage.setMessage(merchantEx.getMessage());
        merchantEx.printStackTrace();
        return new ResponseEntity<>(responseErrorMessage, HttpStatus.valueOf(responseErrorMessage.getStatusCode()));
    }
}
