package org.example.taskproject.controller;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.taskproject.dto.ExceptionDto;
import org.example.taskproject.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return new ExceptionDto("UNEXPECTED_EXCEPTION");
    }


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionDto handleNotFound(NotFoundException ex) {
        log.warn("Not found: {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return new ExceptionDto(ex.getMessage());
    }


    @ExceptionHandler({
            AccessDeniedException.class,
            AlreadyExistException.class,
            CardAlreadyBlockedException.class,
            CardAlreadyDeletedException.class,
            CardInactiveException.class,
            BadCredentialsException.class,
            PasswordMismatchException.class,
            UnsufficientBalanceException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleBadRequest(RuntimeException ex) {
        log.warn("Bad request: {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return new ExceptionDto(ex.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<ExceptionDto> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        return ex.getAllErrors()
                .stream()
                .map(error -> new ExceptionDto(error.getDefaultMessage()))
                .toList();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<ExceptionDto> handleConstraintViolations(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ex.getConstraintViolations()
                .stream()
                .map(v -> new ExceptionDto(v.getMessage()))
                .toList();
    }
}
