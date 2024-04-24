package com.example.userhandler.exception;

public class UserBadRequestException extends RuntimeException {
    public UserBadRequestException(String message) {
        super(message);
    }

    public UserBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
