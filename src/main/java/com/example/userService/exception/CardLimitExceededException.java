package com.example.userService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CardLimitExceededException extends RuntimeException {
    public CardLimitExceededException(String message) {
        super(message);
    }

    public CardLimitExceededException(Long userId) {
        super("User with id " + userId + " cannot have more than 5 active cards");
    }
}
