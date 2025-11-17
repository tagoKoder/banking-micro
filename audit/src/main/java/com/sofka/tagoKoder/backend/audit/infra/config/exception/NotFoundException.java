package com.sofka.tagoKoder.backend.audit.infra.config.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}