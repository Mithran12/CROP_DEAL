package com.cropdeal.crop.exception;

public class CropAccessDeniedException extends RuntimeException {

    public CropAccessDeniedException(String message) {
        super(message);
    }
}