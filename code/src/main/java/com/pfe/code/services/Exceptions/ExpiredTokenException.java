package com.pfe.code.services.Exceptions;

public class ExpiredTokenException extends RuntimeException{
    private String message;
    public ExpiredTokenException(String message){
        super(message);
    }
}
