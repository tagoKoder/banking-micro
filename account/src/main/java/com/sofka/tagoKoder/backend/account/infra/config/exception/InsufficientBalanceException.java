package com.sofka.tagoKoder.backend.account.infra.config.exception;

public class InsufficientBalanceException extends RuntimeException{
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
