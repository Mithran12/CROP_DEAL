package com.cropdeal.order.exception;

public class UnauthorizedResourceException extends RuntimeException {

    public UnauthorizedResourceException(String message) {
        super(message);
    }
}