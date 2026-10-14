package com.app.AutomationSales.exceptionHandling;

/** Thrown when the request is well-formed JSON but breaks a business rule (returns HTTP 400). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
