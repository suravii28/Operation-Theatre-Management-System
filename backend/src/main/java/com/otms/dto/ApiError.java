package com.otms.dto;

import java.time.Instant;
import java.util.List;

public record ApiError(
        int status,
        String message,
        List<String> details,
        Instant timestamp
) {

    public static ApiError of(int status, String message, List<String> details) {
        return new ApiError(status, message, details, Instant.now());
    }

    public static ApiError of(int status, String message) {
        return of(status, message, List.of());
    }
}
