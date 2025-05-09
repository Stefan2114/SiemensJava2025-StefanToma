package com.siemens.internship.Exceptions;

public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super("Service error: " + message);
    }

}
