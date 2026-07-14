package com.pfe.code.services.Exceptions;

public class GlobalException extends RuntimeException{
    private String message;

    public GlobalException(String message) {
        super(message);
    }
}
