package com.shoppix.cart_reactive_service.exception;

public class CartServiceException extends RuntimeException{
    public CartServiceException() {
        super();
    }

    public CartServiceException(String message) {
        super(message);
    }

    public CartServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CartServiceException(Throwable cause) {
        super(cause);
    }

    protected CartServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
