package com.study.controller_advice.exception.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExceptionResponse {
    private final String message;

    public static ExceptionResponse from(String message) {
        return new ExceptionResponse(message);
    }
}
