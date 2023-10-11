package com.meru.order_service.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public class OrderServiceException extends RuntimeException{

    public OrderServiceException() {
        super();
    }

    public OrderServiceException(String message) {
        super(message);
    }

    public OrderServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderServiceException(Throwable cause) {
        super(cause);
    }

    protected OrderServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
