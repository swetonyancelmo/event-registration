package com.swetonyancelmo.event_registration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRequestParameterException extends RuntimeException {
    public InvalidRequestParameterException(String message) {
        super(message);
    }
}
