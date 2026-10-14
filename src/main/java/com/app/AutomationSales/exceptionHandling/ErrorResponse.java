package com.app.AutomationSales.exceptionHandling;

import java.time.Instant;
import java.util.Map;

/**
 * Every error the API returns has this same shape, e.g.
 * {
 *   "timestamp": "2026-10-06T08:00:00Z",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "path": "/api/customers",
 *   "fieldErrors": { "customerEmail": "customerEmail must be a valid email address" }
 * }
 * fieldErrors is only filled for validation errors.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {}
