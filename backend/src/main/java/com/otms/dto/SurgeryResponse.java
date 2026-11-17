package com.otms.dto;

import com.otms.model.SurgeryStatus;

import java.time.LocalDateTime;

public record SurgeryResponse(
        Long id,
        String patientName,
        String procedureName,
        Long leadSurgeonId,
        String leadSurgeonName,
        Long theatreId,
        String theatreName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        SurgeryStatus status,
        boolean emergency,
        String notes
) {
}
