package com.github.hikvision.api.exceptions;

public class EmployeeAlreadyExist extends RuntimeException{
    public EmployeeAlreadyExist(String message) {
        super(message);
    }

    public EmployeeAlreadyExist(String message, Throwable cause) {
        super(message, cause);
    }
}
