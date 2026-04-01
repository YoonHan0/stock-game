package com.stockgame.service;

public class QuizVoteConflictException extends RuntimeException {

    private final String code;

    public QuizVoteConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

