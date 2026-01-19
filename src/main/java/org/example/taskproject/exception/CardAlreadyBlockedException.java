package org.example.taskproject.exception;

public class CardAlreadyBlockedException extends RuntimeException{
    public CardAlreadyBlockedException(String message){
        super(message);
    }
}
