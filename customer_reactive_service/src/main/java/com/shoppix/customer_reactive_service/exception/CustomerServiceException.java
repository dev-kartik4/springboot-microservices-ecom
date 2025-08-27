package com.shoppix.customer_reactive_service.exception;

public class CustomerServiceException extends RuntimeException {

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
