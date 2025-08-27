package com.shoppix.checkout_reactive_service.exception;

public class CheckoutServiceException extends RuntimeException{

public CheckoutServiceException() {
        super();
        }

public CheckoutServiceException(String message) {
        super(message);
        }

public CheckoutServiceException(String message, Throwable cause) {
        super(message, cause);
        }

public CheckoutServiceException(Throwable cause) {
        super(cause);
        }

protected CheckoutServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        }
}
