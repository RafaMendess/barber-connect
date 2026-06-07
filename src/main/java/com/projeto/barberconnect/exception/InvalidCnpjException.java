package com.projeto.barberconnect.exception;

public class InvalidCnpjException extends RuntimeException {

    public InvalidCnpjException(String cnpj) {
        super("CNPJ inválido: " + cnpj);
    }
}
