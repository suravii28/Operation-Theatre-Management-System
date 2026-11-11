package com.otms.service;

import com.otms.dto.RescheduleRequest;
import com.otms.dto.SurgeryRequest;
import com.otms.dto.SurgeryResponse;
import com.otms.model.Surgery;
import com.otms.model.SurgeryStatus;
import com.otms.model.Theatre;
import com.otms.model.UserAccount;
import com.otms.repository.SurgeryRepository;
import com.otms.repository.TheatreRepository;
import com.otms.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class SchedulingService {

    private static final List<SurgeryStatus> ACTIVE_STATUSES =
            List.of(SurgeryStatus.SCHEDULED, SurgeryStatus.IN_PROGRESS);

    private final SurgeryRepository surgeryRepository;
    private final TheatreRepository theatreRepository;
    private final UserAccountRepository userAccountRepository;

    public SchedulingService(SurgeryRepository surgeryRepository,
                             TheatreRepository theatreRepository,
                             UserAccountRepository userAccountRepository) {
        this.surgeryRepository = surgeryRepository;
        this.theatreRepository = theatreRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public SurgeryResponse schedule(SurgeryRequest request) {
        if (!request.isEndAfterStart()) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Pessimistic row locks (theatre first, then surgeon) serialise overlapping
        // scheduling attempts so concurrent requests can never double-book a slot
        // (OTMS-NF-006). The lock order is fixed to avoid deadlocks.
        Theatre theatre = theatreRepository.findByIdForUpdate(request.theatreId())
                .filter(Theatre::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found: " + request.theatreId()));

        UserAccount surgeon = userAccountRepository.findByIdForUpdate(request.leadSurgeonId())
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Surgeon not found: " + request.leadSurgeonId()));

        List<Surgery> conflicts = findConflicts(request.theatreId(), request.leadSurgeonId(),
                request.startTime(), request.endTime());

        if (!conflicts.isEmpty()) {
            throw new SchedulingConflictException(describeConflicts(conflicts));
        }

        Surgery surgery = new Surgery();
        surgery.setPatientName(request.patientName());
        surgery.setProcedureName(request.procedureName());
        surgery.setLeadSurgeon(surgeon);
        surgery.setTheatre(theatre);
        surgery.setStartTime(request.startTime());
        surgery.setEndTime(request.endTime());
        surgery.setEmergency(request.emergency());
        surgery.setNotes(request.notes());
        surgery.setStatus(SurgeryStatus.SCHEDULED);

        return toResponse(surgeryRepository.save(surgery));
    }

    @Transactional(readOnly = true)
    public List<Surgery> findConflicts(Long theatreId, Long surgeonId, LocalDateTime start, LocalDateTime end) {
        return surgeryRepository.findOverlapping(ACTIVE_STATUSES, start, end).stream()
                .filter(s -> s.getTheatre().getId().equals(theatreId)
                        || s.getLeadSurgeon().getId().equals(surgeonId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SurgeryResponse> list(LocalDate date, Long theatreId, SurgeryStatus status) {
        LocalDateTime from = date != null ? date.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime to = date != null ? date.atTime(LocalTime.MAX) : LocalDateTime.of(2999, 12, 31, 23, 59);

        return surgeryRepository.findByStartTimeBetweenOrderByStartTime(from, to).stream()
                .filter(s -> theatreId == null || s.getTheatre().getId().equals(theatreId))
                .filter(s -> status == null || s.getStatus() == status)
                .sorted(Comparator.comparing(Surgery::getStartTime))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SurgeryResponse get(Long id) {
        return surgeryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Surgery not found: " + id));
    }

    @Transactional
    public SurgeryResponse updateStatus(Long id, SurgeryStatus status) {
        Surgery surgery = surgeryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surgery not found: " + id));
        surgery.setStatus(status);
        return toResponse(surgeryRepository.save(surgery));
    }

    @Transactional(readOnly = true)
    public List<SurgeryResponse> listEmergencies() {
        return surgeryRepository.findByEmergencyTrueAndStatusInOrderByStartTime(ACTIVE_STATUSES).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * OTMS-F-017: for an emergency surgery, identify the non-emergency active
     * surgeries it displaces (same theatre or same lead surgeon, overlapping
     * time window).
     */
    @Transactional(readOnly = true)
    public List<SurgeryResponse> impactAnalysis(Long emergencyId) {
        Surgery emergency = surgeryRepository.findById(emergencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Surgery not found: " + emergencyId));
        if (!emergency.isEmergency()) {
            throw new IllegalArgumentException("Surgery " + emergencyId + " is not an emergency case");
        }

        return findConflicts(emergency.getTheatre().getId(), emergency.getLeadSurgeon().getId(),
                        emergency.getStartTime(), emergency.getEndTime()).stream()
                .filter(s -> !s.getId().equals(emergencyId))
                .filter(s -> !s.isEmergency())
                .map(this::toResponse)
                .toList();
    }

    /**
     * OTMS-F-018: move a (displaced) surgery to a new theatre/time slot after
     * re-validating theatre, surgeon and overlap rules. The surgery itself is
     * excluded from the conflict check.
     */
    @Transactional
    public SurgeryResponse reschedule(Long id, RescheduleRequest request) {
        if (!request.isEndAfterStart()) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Surgery surgery = surgeryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surgery not found: " + id));

        // Same lock order as schedule(): theatre row first, then surgeon row.
        Theatre theatre = theatreRepository.findByIdForUpdate(request.theatreId())
                .filter(Theatre::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found: " + request.theatreId()));

        UserAccount surgeon = userAccountRepository.findByIdForUpdate(surgery.getLeadSurgeon().getId())
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Surgeon not found: " + surgery.getLeadSurgeon().getId()));

        List<Surgery> conflicts = findConflicts(request.theatreId(), surgeon.getId(),
                request.startTime(), request.endTime()).stream()
                .filter(s -> !s.getId().equals(id))
                .toList();

        if (!conflicts.isEmpty()) {
            throw new SchedulingConflictException(describeConflicts(conflicts));
        }

        surgery.setTheatre(theatre);
        surgery.setStartTime(request.startTime());
        surgery.setEndTime(request.endTime());
        return toResponse(surgeryRepository.save(surgery));
    }

    private String describeConflicts(List<Surgery> conflicts) {
        StringBuilder message = new StringBuilder("Scheduling conflict detected:");
        for (Surgery s : conflicts) {
            message.append(String.format(" [%s occupied by '%s' for %s (%s - %s)]",
                    s.getTheatre().getName(),
                    s.getPatientName(),
                    s.getLeadSurgeon().getName(),
                    s.getStartTime(),
                    s.getEndTime()));
        }
        return message.toString();
    }

    private SurgeryResponse toResponse(Surgery s) {
        return new SurgeryResponse(
                s.getId(),
                s.getPatientName(),
                s.getProcedureName(),
                s.getLeadSurgeon().getId(),
                s.getLeadSurgeon().getName(),
                s.getTheatre().getId(),
                s.getTheatre().getName(),
                s.getStartTime(),
                s.getEndTime(),
                s.getStatus(),
                s.isEmergency(),
                s.getNotes()
        );
    }
}
