package com.pfe.code.services.Exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    private String message;
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
