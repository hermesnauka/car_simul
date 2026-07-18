package com.jsystems.carsimul.exception;

public class ExamNotActiveException extends RuntimeException {
    public ExamNotActiveException(Long sessionId, String status) {
        super("Exam session " + sessionId + " is not active (status: " + status + ")");
    }
}
