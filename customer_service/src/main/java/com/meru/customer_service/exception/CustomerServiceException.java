package com.meru.customer_service.exception;

import com.meru.customer_service.model.ResponseErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public class CustomerServiceException extends RuntimeException {

    @Autowired
    private HttpStatus httpStatus;

    public CustomerServiceException(){
    }

    public CustomerServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomerServiceException(Throwable cause) {
        super(cause);
    }

    protected CustomerServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CustomerServiceException(String message) {
        super(message);
    }
}
