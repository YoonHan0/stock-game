package com.stockgame.service;
public class AuthBadRequestException extends RuntimeException {
    public AuthBadRequestException(String message) {
        super(message);
    }
}
