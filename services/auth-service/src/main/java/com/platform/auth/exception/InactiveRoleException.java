package com.platform.auth.exception;

public class InactiveRoleException extends RuntimeException {

    public InactiveRoleException(String message) {
        super(message);
    }
}