package com.shoppix.inventory_service_reactive.exception;

public class InventoryServiceException extends RuntimeException{
    public InventoryServiceException() {
        super();
    }

    public InventoryServiceException(String message) {
        super(message);
    }

    public InventoryServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventoryServiceException(Throwable cause) {
        super(cause);
    }

    protected InventoryServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
