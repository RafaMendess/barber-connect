package com.projeto.barberconnect.exception;

public class CnpjAlreadyExistsException extends RuntimeException {

    public CnpjAlreadyExistsException(String cnpj) {
        super("CNPJ já cadastrado: " + cnpj);
    }
}
