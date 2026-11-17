package com.otms.dto;

import com.otms.model.SurgeryStatus;

import java.time.LocalDateTime;
import java.util.List;

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
        String notes,
        List<Long> equipmentIds,
        List<String> equipmentNames
) {
}
