package com.projeto.barberconnect.exception;

public class EmailNotVerifiedException extends RuntimeException{
    public EmailNotVerifiedException(String message){
        super(message);
    }
}
