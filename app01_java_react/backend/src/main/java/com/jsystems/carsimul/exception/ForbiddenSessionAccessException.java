package com.jsystems.carsimul.exception;

public class ForbiddenSessionAccessException extends RuntimeException {
    public ForbiddenSessionAccessException(Long sessionId) {
        super("Exam session " + sessionId + " does not belong to the authenticated user");
    }
}
