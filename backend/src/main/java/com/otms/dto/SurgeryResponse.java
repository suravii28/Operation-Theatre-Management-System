
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

                // Newly added: assigned nurse details
                Long assignedNurseId,
                String assignedNurseName,

                LocalDateTime startTime,
                LocalDateTime endTime,
                SurgeryStatus status,
                boolean emergency,
                String notes) {
}
