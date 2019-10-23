package com.mattech.on_call.exceptions;

public class UpdateNotScheduledException extends Exception {
    public UpdateNotScheduledException(String message, Throwable cause) {
        super(message, cause);
    }
}
