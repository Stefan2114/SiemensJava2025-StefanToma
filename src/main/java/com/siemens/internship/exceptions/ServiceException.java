package com.siemens.internship.exceptions;

public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super("Service error: " + message);
    }

    public ServiceException(String message, Throwable cause) {
        super("Service error: " + message, cause);
    }

}
