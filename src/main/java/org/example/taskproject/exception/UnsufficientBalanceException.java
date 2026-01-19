package org.example.taskproject.exception;

public class UnsufficientBalanceException extends RuntimeException {
    public UnsufficientBalanceException(String message) {
        super(message);
    }
}
