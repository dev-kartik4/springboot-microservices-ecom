package com.shoppix.merchant_reactive_service.exception;

public class MerchantServiceException extends RuntimeException{
    public MerchantServiceException() {
        super();
    }

    public MerchantServiceException(String message) {
        super(message);
    }

    public MerchantServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MerchantServiceException(Throwable cause) {
        super(cause);
    }

    protected MerchantServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
