package com.pfe.code.services.Exceptions;

public class InvalidTokenException extends RuntimeException{
    private String message;
    public InvalidTokenException(String message){
        super(message);
    }
}
