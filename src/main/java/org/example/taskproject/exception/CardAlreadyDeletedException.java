package org.example.taskproject.exception;

public class CardAlreadyDeletedException extends RuntimeException {
    public CardAlreadyDeletedException(String message) {
        super(message);
    }
}
