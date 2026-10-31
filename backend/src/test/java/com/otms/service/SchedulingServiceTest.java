package com.otms.service;

import com.otms.dto.SurgeryRequest;
import com.otms.dto.SurgeryResponse;
import com.otms.model.*;
import com.otms.repository.SurgeryRepository;
import com.otms.repository.TheatreRepository;
import com.otms.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock
    private SurgeryRepository surgeryRepository;

    @Mock
    private TheatreRepository theatreRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private SchedulingService schedulingService;

    private Theatre ot1;
    private Theatre ot2;
    private UserAccount drRam;

    @BeforeEach
    void setUp() {
        ot1 = theatre(1L, "OT-1");
        ot2 = theatre(2L, "OT-2");
        drRam = surgeon(10L, "Dr. Ram Sharma");
    }

    @Test
    void schedulesSurgeryWhenNoConflict() {
        SurgeryRequest request = request(1L, 10L,
                LocalDateTime.of(2030, 1, 10, 9, 0),
                LocalDateTime.of(2030, 1, 10, 11, 0));
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(ot1));
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(drRam));
        when(surgeryRepository.findOverlapping(anyList(), any(), any())).thenReturn(List.of());
        when(surgeryRepository.save(any(Surgery.class))).thenAnswer(invocation -> {
            Surgery s = invocation.getArgument(0);
            ReflectionTestUtils.setField(s, "id", 99L);
            return s;
        });

        SurgeryResponse response = schedulingService.schedule(request);

        assertThat(response.id()).isEqualTo(99L);
        verify(surgeryRepository).save(any(Surgery.class));
    }

    @Test
    void rejectsOverlappingSurgeryInSameTheatre() {
        SurgeryRequest request = request(1L, 10L,
                LocalDateTime.of(2030, 1, 10, 10, 0),
                LocalDateTime.of(2030, 1, 10, 12, 0));
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(ot1));
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(drRam));
        when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                .thenReturn(List.of(existingSurgery(50L, ot1, drRam,
                        LocalDateTime.of(2030, 1, 10, 9, 0),
                        LocalDateTime.of(2030, 1, 10, 11, 0))));

        assertThatThrownBy(() -> schedulingService.schedule(request))
                .isInstanceOf(SchedulingConflictException.class)
                .hasMessageContaining("Scheduling conflict detected");
    }

    @Test
    void rejectsOverlappingSurgeryForSameSurgeonInDifferentTheatre() {
        SurgeryRequest request = request(2L, 10L,
                LocalDateTime.of(2030, 1, 10, 10, 0),
                LocalDateTime.of(2030, 1, 10, 12, 0));
        when(theatreRepository.findById(2L)).thenReturn(Optional.of(ot2));
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(drRam));
        when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                .thenReturn(List.of(existingSurgery(51L, ot1, drRam,
                        LocalDateTime.of(2030, 1, 10, 9, 0),
                        LocalDateTime.of(2030, 1, 10, 11, 0))));

        assertThatThrownBy(() -> schedulingService.schedule(request))
                .isInstanceOf(SchedulingConflictException.class);
    }

    @Test
    void allowsNonOverlappingSurgeryInSameTheatre() {
        SurgeryRequest request = request(1L, 10L,
                LocalDateTime.of(2030, 1, 10, 12, 0),
                LocalDateTime.of(2030, 1, 10, 13, 0));
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(ot1));
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(drRam));
        when(surgeryRepository.findOverlapping(anyList(), any(), any())).thenReturn(List.of());
        when(surgeryRepository.save(any(Surgery.class))).thenAnswer(invocation -> {
            Surgery s = invocation.getArgument(0);
            ReflectionTestUtils.setField(s, "id", 100L);
            return s;
        });

        SurgeryResponse response = schedulingService.schedule(request);

        assertThat(response.id()).isEqualTo(100L);
    }

    @Test
    void rejectsEndTimeBeforeStartTime() {
        SurgeryRequest request = request(1L, 10L,
                LocalDateTime.of(2030, 1, 10, 11, 0),
                LocalDateTime.of(2030, 1, 10, 9, 0));

        assertThatThrownBy(() -> schedulingService.schedule(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be after start time");
    }

    @Test
    void conflictCheckOnlyConsidersActiveStatuses() {
        SurgeryRequest request = request(1L, 10L,
                LocalDateTime.of(2030, 1, 10, 9, 0),
                LocalDateTime.of(2030, 1, 10, 11, 0));
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(ot1));
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(drRam));
        when(surgeryRepository.findOverlapping(anyList(), any(), any())).thenReturn(List.of());
        when(surgeryRepository.save(any(Surgery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        schedulingService.schedule(request);

        verify(surgeryRepository).findOverlapping(
                argThat(statuses -> statuses.containsAll(
                        List.of(SurgeryStatus.SCHEDULED, SurgeryStatus.IN_PROGRESS))),
                any(), any());
    }

    private SurgeryRequest request(Long theatreId, Long surgeonId, LocalDateTime start, LocalDateTime end) {
        return new SurgeryRequest("Patient X", "Appendectomy", surgeonId, theatreId, start, end, false, null);
    }

    private Theatre theatre(Long id, String name) {
        Theatre t = new Theatre(name, "General");
        ReflectionTestUtils.setField(t, "id", id);
        return t;
    }

    private UserAccount surgeon(Long id, String name) {
        UserAccount u = new UserAccount("doc@otms.local", "x", name, Role.DOCTOR);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private Surgery existingSurgery(Long id, Theatre theatre, UserAccount surgeon,
                                    LocalDateTime start, LocalDateTime end) {
        Surgery s = new Surgery();
        ReflectionTestUtils.setField(s, "id", id);
        s.setPatientName("Existing Patient");
        s.setProcedureName("Existing Procedure");
        s.setLeadSurgeon(surgeon);
        s.setTheatre(theatre);
        s.setStartTime(start);
        s.setEndTime(end);
        s.setStatus(SurgeryStatus.SCHEDULED);
        return s;
    }
}
