package com.app.AutomationSales.exceptionHandling;

public class ResourceNotFoundExceptionHandler extends RuntimeException {

public ResourceNotFoundExceptionHandler(String message) {
    super(message);
}
}
