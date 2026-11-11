package com.otms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RescheduleRequest(
        @NotNull Long theatreId,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startTime,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endTime
) {

    @AssertTrue(message = "End time must be after start time")
    public boolean isEndAfterStart() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}
