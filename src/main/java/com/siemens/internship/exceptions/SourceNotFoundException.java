package com.siemens.internship.exceptions;

public class SourceNotFoundException extends RuntimeException {

    public SourceNotFoundException(String message) {
        super("Source not found: " + message);
    }

    public SourceNotFoundException(String message, Throwable cause) {
        super("Source not found: " + message, cause);
    }

}
