package com.sofka.tagoKoder.backend.account.infra.config.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}