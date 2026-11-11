package com.otms.service;

import com.otms.dto.RescheduleRequest;
import com.otms.dto.SurgeryRequest;
import com.otms.dto.SurgeryResponse;
import com.otms.model.Equipment;
import com.otms.model.NotificationType;
import com.otms.model.Surgery;
import com.otms.model.SurgeryStatus;
import com.otms.model.Theatre;
import com.otms.model.UserAccount;
import com.otms.repository.EquipmentRepository;
import com.otms.repository.SurgeryRepository;
import com.otms.repository.TheatreRepository;
import com.otms.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SchedulingService {

    private static final List<SurgeryStatus> ACTIVE_STATUSES =
            List.of(SurgeryStatus.SCHEDULED, SurgeryStatus.IN_PROGRESS);

    private final SurgeryRepository surgeryRepository;
    private final TheatreRepository theatreRepository;
    private final UserAccountRepository userAccountRepository;
    private final EquipmentRepository equipmentRepository;
    private final NotificationService notificationService;

    public SchedulingService(SurgeryRepository surgeryRepository,
                             TheatreRepository theatreRepository,
                             UserAccountRepository userAccountRepository,
                             EquipmentRepository equipmentRepository,
                             NotificationService notificationService) {
        this.surgeryRepository = surgeryRepository;
        this.theatreRepository = theatreRepository;
        this.userAccountRepository = userAccountRepository;
        this.equipmentRepository = equipmentRepository;
        this.notificationService = notificationService;
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

        // Lock order: theatre → surgeon → equipment (ascending ids) — see lockEquipment.
        Set<Equipment> equipment = lockEquipment(request.equipmentIds());

        List<Surgery> overlapping = surgeryRepository.findOverlapping(
                ACTIVE_STATUSES, request.startTime(), request.endTime());

        List<Surgery> conflicts = theatreOrSurgeonConflicts(overlapping, request.theatreId(), request.leadSurgeonId());

        if (!conflicts.isEmpty()) {
            throw new SchedulingConflictException(describeConflicts(conflicts));
        }

        checkEquipmentAvailability(equipment, overlapping, null);

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
        surgery.setEquipment(equipment);

        Surgery saved = surgeryRepository.save(surgery);

        // OTMS-F-019 / OTMS-F-020: notify relevant users about schedule events.
        notificationService.record(
                request.emergency() ? NotificationType.EMERGENCY_SCHEDULED : NotificationType.SURGERY_SCHEDULED,
                saved.getId(),
                String.format("%s '%s' for %s in %s (%s - %s)",
                        request.emergency() ? "Emergency surgery scheduled:" : "Surgery scheduled:",
                        saved.getProcedureName(), saved.getPatientName(), theatre.getName(),
                        saved.getStartTime(), saved.getEndTime()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<Surgery> findConflicts(Long theatreId, Long surgeonId, LocalDateTime start, LocalDateTime end) {
        return theatreOrSurgeonConflicts(
                surgeryRepository.findOverlapping(ACTIVE_STATUSES, start, end), theatreId, surgeonId);
    }

    private List<Surgery> theatreOrSurgeonConflicts(List<Surgery> overlapping, Long theatreId, Long surgeonId) {
        return overlapping.stream()
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
        Surgery saved = surgeryRepository.save(surgery);

        notificationService.record(
                NotificationType.SURGERY_STATUS_CHANGED,
                saved.getId(),
                String.format("Surgery '%s' for %s is now %s",
                        saved.getProcedureName(), saved.getPatientName(), status));

        return toResponse(saved);
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

        // Same lock order as schedule(): theatre row, then surgeon row.
        Theatre theatre = theatreRepository.findByIdForUpdate(request.theatreId())
                .filter(Theatre::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found: " + request.theatreId()));

        UserAccount surgeon = userAccountRepository.findByIdForUpdate(surgery.getLeadSurgeon().getId())
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Surgeon not found: " + surgery.getLeadSurgeon().getId()));

        // The surgery keeps its required equipment; re-lock and re-validate it
        // against the new slot (the surgery itself is excluded from the check).
        Set<Equipment> equipment = lockEquipment(equipmentIds(surgery));

        List<Surgery> overlapping = surgeryRepository.findOverlapping(
                ACTIVE_STATUSES, request.startTime(), request.endTime());

        List<Surgery> conflicts = theatreOrSurgeonConflicts(overlapping, request.theatreId(), surgeon.getId()).stream()
                .filter(s -> !s.getId().equals(id))
                .toList();

        if (!conflicts.isEmpty()) {
            throw new SchedulingConflictException(describeConflicts(conflicts));
        }

        checkEquipmentAvailability(equipment, overlapping, id);

        surgery.setTheatre(theatre);
        surgery.setStartTime(request.startTime());
        surgery.setEndTime(request.endTime());
        Surgery saved = surgeryRepository.save(surgery);

        notificationService.record(
                NotificationType.SURGERY_RESCHEDULED,
                saved.getId(),
                String.format("Surgery '%s' for %s rescheduled to %s (%s - %s)%s",
                        saved.getProcedureName(), saved.getPatientName(), theatre.getName(),
                        saved.getStartTime(), saved.getEndTime(),
                        saved.isEmergency() ? " — emergency case" : ""));

        return toResponse(saved);
    }

    /**
     * OTMS-F-012: pessimistically lock the requested equipment rows and verify
     * they all exist. Locks are taken after the theatre and surgeon locks and
     * in ascending id order, keeping the global lock order deadlock-free.
     */
    private Set<Equipment> lockEquipment(List<Long> equipmentIds) {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<Long> ids = equipmentIds.stream().distinct().sorted().toList();
        List<Equipment> found = equipmentRepository.findAllByIdForUpdate(ids);
        if (found.size() != ids.size()) {
            Set<Long> foundIds = found.stream().map(Equipment::getId).collect(Collectors.toSet());
            List<Long> missing = ids.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new ResourceNotFoundException("Equipment not found: " + missing);
        }
        return new LinkedHashSet<>(found);
    }

    /**
     * OTMS-F-012 / OTMS-F-022: equipment required by a surgery must be flagged
     * available and must not be in use by another active surgery overlapping
     * the requested window. The record being edited (excludeId) is ignored.
     */
    private void checkEquipmentAvailability(Set<Equipment> equipment, List<Surgery> overlapping, Long excludeId) {
        if (equipment.isEmpty()) {
            return;
        }

        List<Equipment> unavailable = equipment.stream().filter(e -> !e.isAvailable()).toList();
        if (!unavailable.isEmpty()) {
            String names = unavailable.stream().map(Equipment::getName).collect(Collectors.joining(", "));
            throw new SchedulingConflictException(
                    "Scheduling conflict detected: equipment in maintenance: " + names);
        }

        Set<Long> requestedIds = equipment.stream().map(Equipment::getId).collect(Collectors.toSet());
        List<Surgery> inUse = overlapping.stream()
                .filter(s -> !s.getId().equals(excludeId))
                .filter(s -> s.getEquipment().stream().anyMatch(e -> requestedIds.contains(e.getId())))
                .toList();
        if (!inUse.isEmpty()) {
            throw new SchedulingConflictException(describeEquipmentConflicts(inUse));
        }
    }

    private List<Long> equipmentIds(Surgery surgery) {
        return surgery.getEquipment().stream().map(Equipment::getId).sorted().toList();
    }

    private String describeEquipmentConflicts(List<Surgery> conflicts) {
        StringBuilder message = new StringBuilder("Scheduling conflict detected: equipment already in use");
        for (Surgery s : conflicts) {
            message.append(String.format(" [%s used by '%s' for %s (%s - %s)]",
                    s.getEquipment().stream().map(Equipment::getName).collect(Collectors.joining(", ")),
                    s.getPatientName(),
                    s.getLeadSurgeon().getName(),
                    s.getStartTime(),
                    s.getEndTime()));
        }
        return message.toString();
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
                s.getNotes(),
                s.getEquipment().stream().map(Equipment::getId).sorted().toList(),
                s.getEquipment().stream().map(Equipment::getName).sorted().toList()
        );
    }
}
