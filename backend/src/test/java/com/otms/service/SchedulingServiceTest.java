
package com.otms.service;

import com.otms.dto.SurgeryRequest;
import com.otms.dto.SurgeryResponse;
import com.otms.dto.RescheduleRequest;
import com.otms.model.*;
import com.otms.repository.EquipmentRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

        @Mock
        private EquipmentRepository equipmentRepository;

        @Mock
        private NotificationService notificationService;

        @InjectMocks
        private SchedulingService schedulingService;

        private Theatre ot1;
        private Theatre ot2;
        private UserAccount drRam;
        private UserAccount drPriya;
        private UserAccount nurse;

        @BeforeEach
        void setUp() {
                ot1 = theatre(1L, "OT-1");
                ot2 = theatre(2L, "OT-2");
                drRam = staff(10L, "Dr. Ram Sharma", Role.DOCTOR);
                drPriya = staff(11L, "Dr. Priya", Role.DOCTOR);
                nurse = staff(20L, "Nurse A", Role.NURSE);
        }

        @Test
        void schedulesSurgeryWhenNoConflict() {
                SurgeryRequest request = request(1L, 10L,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                mockStaffAndTheatre(1L, drRam);
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of());
                when(surgeryRepository.save(any(Surgery.class))).thenAnswer(invocation -> {
                        Surgery s = invocation.getArgument(0);
                        ReflectionTestUtils.setField(s, "id", 99L);
                        return s;
                });

                SurgeryResponse response = schedulingService.schedule(request);

                assertThat(response.id()).isEqualTo(99L);
                assertThat(response.assignedNurseId()).isEqualTo(20L);
                verify(surgeryRepository).save(any(Surgery.class));
        }

        @Test
        void rejectsOverlappingSurgeryInSameTheatre() {
                SurgeryRequest request = request(1L, 10L,
                                LocalDateTime.of(2030, 1, 10, 10, 0),
                                LocalDateTime.of(2030, 1, 10, 12, 0));

                mockStaffAndTheatre(1L, drRam);
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

                mockStaffAndTheatre(2L, drRam);
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of(existingSurgery(51L, ot1, drRam,
                                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                                LocalDateTime.of(2030, 1, 10, 11, 0))));

                assertThatThrownBy(() -> schedulingService.schedule(request))
                                .isInstanceOf(SchedulingConflictException.class);
        }

        @Test
        void rejectsOverlappingSurgeryForSameNurseInDifferentTheatre() {
                SurgeryRequest request = request(2L, 11L,
                                LocalDateTime.of(2030, 1, 10, 10, 0),
                                LocalDateTime.of(2030, 1, 10, 12, 0));

                mockStaffAndTheatre(2L, drPriya);

                Surgery existing = existingSurgery(52L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of(existing));

                assertThatThrownBy(() -> schedulingService.schedule(request))
                                .isInstanceOf(SchedulingConflictException.class)
                                .hasMessageContaining("Scheduling conflict detected");
        }

        @Test
        void allowsNonOverlappingSurgeryInSameTheatre() {
                SurgeryRequest request = request(1L, 10L,
                                LocalDateTime.of(2030, 1, 10, 12, 0),
                                LocalDateTime.of(2030, 1, 10, 13, 0));

                mockStaffAndTheatre(1L, drRam);
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of());
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

                mockStaffAndTheatre(1L, drRam);
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of());
                when(surgeryRepository.save(any(Surgery.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                schedulingService.schedule(request);

                verify(surgeryRepository, times(1)).findOverlapping(
                                argThat(statuses -> statuses.containsAll(
                                                List.of(SurgeryStatus.SCHEDULED, SurgeryStatus.IN_PROGRESS))),
                                any(), any());
        }

        // Updated for Issue #18: inactive theatres now cause a scheduling conflict.
        @Test
        void rejectsSchedulingInInactiveTheatre() {
                Theatre inactive = theatre(1L, "OT-1");
                inactive.setActive(false);

                SurgeryRequest request = request(1L, 10L,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                when(theatreRepository.findByIdForUpdate(1L))
                                .thenReturn(Optional.of(inactive));

                assertThatThrownBy(() -> schedulingService.schedule(request))
                                .isInstanceOf(SchedulingConflictException.class)
                                .hasMessageContaining("OT-1")
                                .hasMessageContaining("currently unavailable");

                verify(surgeryRepository, never()).save(any(Surgery.class));
        }

        @Test
        void impactAnalysisFindsDisplacedNonEmergencySurgeries() {
                Surgery emergency = existingSurgery(60L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));
                emergency.setEmergency(true);

                Surgery routine = existingSurgery(61L, ot1, drPriya,
                                LocalDateTime.of(2030, 1, 10, 10, 0),
                                LocalDateTime.of(2030, 1, 10, 12, 0));

                when(surgeryRepository.findById(60L)).thenReturn(Optional.of(emergency));
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of(routine));

                List<SurgeryResponse> impact = schedulingService.impactAnalysis(60L);

                assertThat(impact).hasSize(1);
                assertThat(impact.get(0).id()).isEqualTo(61L);
        }

        @Test
        void impactAnalysisExcludesItselfAndOtherEmergencies() {
                Surgery emergency = existingSurgery(60L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));
                emergency.setEmergency(true);

                Surgery otherEmergency = existingSurgery(62L, ot1, drPriya,
                                LocalDateTime.of(2030, 1, 10, 10, 0),
                                LocalDateTime.of(2030, 1, 10, 12, 0));
                otherEmergency.setEmergency(true);

                when(surgeryRepository.findById(60L)).thenReturn(Optional.of(emergency));
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of(emergency, otherEmergency));

                assertThat(schedulingService.impactAnalysis(60L)).isEmpty();
        }

        @Test
        void impactAnalysisRejectsNonEmergencySurgery() {
                Surgery routine = existingSurgery(61L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                when(surgeryRepository.findById(61L)).thenReturn(Optional.of(routine));

                assertThatThrownBy(() -> schedulingService.impactAnalysis(61L))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("not an emergency case");
        }

        @Test
        void rescheduleMovesSurgeryToConflictFreeSlot() {
                Surgery displaced = existingSurgery(61L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                when(surgeryRepository.findById(61L)).thenReturn(Optional.of(displaced));
                when(theatreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(ot2));
                when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(drRam));
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of());
                when(surgeryRepository.save(any(Surgery.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                SurgeryResponse response = schedulingService.reschedule(61L,
                                new RescheduleRequest(2L,
                                                LocalDateTime.of(2030, 1, 11, 9, 0),
                                                LocalDateTime.of(2030, 1, 11, 11, 0)));

                assertThat(response.theatreId()).isEqualTo(2L);
                assertThat(response.startTime()).isEqualTo(LocalDateTime.of(2030, 1, 11, 9, 0));
                assertThat(response.endTime()).isEqualTo(LocalDateTime.of(2030, 1, 11, 11, 0));
        }

        @Test
        void rescheduleRejectedWhenNewSlotConflicts() {
                Surgery displaced = existingSurgery(61L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                when(surgeryRepository.findById(61L)).thenReturn(Optional.of(displaced));
                when(theatreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(ot2));
                when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(drRam));
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of(existingSurgery(50L, ot2, drPriya,
                                                LocalDateTime.of(2030, 1, 11, 8, 0),
                                                LocalDateTime.of(2030, 1, 11, 10, 0))));

                assertThatThrownBy(() -> schedulingService.reschedule(61L,
                                new RescheduleRequest(2L,
                                                LocalDateTime.of(2030, 1, 11, 9, 0),
                                                LocalDateTime.of(2030, 1, 11, 11, 0))))
                                .isInstanceOf(SchedulingConflictException.class);
        }

        @Test
        void rescheduleRejectedWhenEndBeforeStart() {
                assertThatThrownBy(() -> schedulingService.reschedule(61L,
                                new RescheduleRequest(2L,
                                                LocalDateTime.of(2030, 1, 11, 11, 0),
                                                LocalDateTime.of(2030, 1, 11, 9, 0))))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("End time must be after start time");
        }

        @Test
        void schedulingRecordsANotificationForTheNewSurgery() {
                SurgeryRequest request = request(1L, 10L,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                mockStaffAndTheatre(1L, drRam);
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of());
                when(surgeryRepository.save(any(Surgery.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                schedulingService.schedule(request);

                verify(notificationService).record(
                                eq(NotificationType.SURGERY_SCHEDULED), any(), anyString());
        }

        @Test
        void emergencySchedulingRecordsAnEmergencyNotification() {
                SurgeryRequest request = new SurgeryRequest(
                                "Patient X", "Emergency laparotomy", 10L, 1L, 20L,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0),
                                true, null, null);

                mockStaffAndTheatre(1L, drRam);
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of());
                when(surgeryRepository.save(any(Surgery.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                schedulingService.schedule(request);

                verify(notificationService).record(
                                eq(NotificationType.EMERGENCY_SCHEDULED), any(), anyString());
        }

        @Test
        void reschedulingRecordsARescheduleNotification() {
                Surgery displaced = existingSurgery(61L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                when(surgeryRepository.findById(61L)).thenReturn(Optional.of(displaced));
                when(theatreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(ot2));
                when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(drRam));
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of());
                when(surgeryRepository.save(any(Surgery.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                schedulingService.reschedule(61L,
                                new RescheduleRequest(2L,
                                                LocalDateTime.of(2030, 1, 11, 9, 0),
                                                LocalDateTime.of(2030, 1, 11, 11, 0)));

                verify(notificationService).record(
                                eq(NotificationType.SURGERY_RESCHEDULED), eq(61L), anyString());
        }

        @Test
        void statusChangeRecordsANotification() {
                Surgery surgery = existingSurgery(61L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                when(surgeryRepository.findById(61L)).thenReturn(Optional.of(surgery));
                when(surgeryRepository.save(any(Surgery.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                schedulingService.updateStatus(61L, SurgeryStatus.IN_PROGRESS);

                verify(notificationService).record(
                                eq(NotificationType.SURGERY_STATUS_CHANGED), eq(61L), anyString());
        }

        @Test
        void schedulesSurgeryWithEquipmentLockingRowsInIdOrder() {
                SurgeryRequest request = request(1L, 10L,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0),
                                List.of(3L, 1L));

                mockStaffAndTheatre(1L, drRam);
                when(equipmentRepository.findAllByIdForUpdate(List.of(1L, 3L)))
                                .thenReturn(List.of(
                                                equipment(1L, "Laparoscope", true),
                                                equipment(3L, "C-arm", true)));
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of());
                when(surgeryRepository.save(any(Surgery.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                SurgeryResponse response = schedulingService.schedule(request);

                verify(equipmentRepository).findAllByIdForUpdate(List.of(1L, 3L));
                assertThat(response.equipmentIds()).containsExactly(1L, 3L);
                assertThat(response.equipmentNames())
                                .containsExactly("C-arm", "Laparoscope");
        }

        @Test
        void rejectsSchedulingWhenEquipmentIsInMaintenance() {
                SurgeryRequest request = request(1L, 10L,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0),
                                List.of(4L));

                mockStaffAndTheatre(1L, drRam);
                when(equipmentRepository.findAllByIdForUpdate(List.of(4L)))
                                .thenReturn(List.of(equipment(4L, "Heart-lung machine", false)));
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of());

                assertThatThrownBy(() -> schedulingService.schedule(request))
                                .isInstanceOf(SchedulingConflictException.class)
                                .hasMessageContaining("equipment in maintenance")
                                .hasMessageContaining("Heart-lung machine");

                verify(surgeryRepository, never()).save(any(Surgery.class));
        }

        @Test
        void rejectsSchedulingWhenEquipmentInUseByOverlappingSurgery() {
                SurgeryRequest request = request(1L, 10L,
                                LocalDateTime.of(2030, 1, 10, 14, 0),
                                LocalDateTime.of(2030, 1, 10, 16, 0),
                                List.of(3L));

                mockStaffAndTheatre(1L, drRam);
                when(equipmentRepository.findAllByIdForUpdate(List.of(3L)))
                                .thenReturn(List.of(equipment(3L, "C-arm", true)));

                Surgery other = existingSurgery(50L, ot2, drPriya,
                                LocalDateTime.of(2030, 1, 10, 13, 30),
                                LocalDateTime.of(2030, 1, 10, 16, 0),
                                equipment(3L, "C-arm", true));
                other.setAssignedNurse(staff(21L, "Nurse B", Role.NURSE));

                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of(other));

                assertThatThrownBy(() -> schedulingService.schedule(request))
                                .isInstanceOf(SchedulingConflictException.class)
                                .hasMessageContaining("equipment already in use");

                verify(surgeryRepository, never()).save(any(Surgery.class));
        }

        @Test
        void rejectsUnknownEquipmentId() {
                SurgeryRequest request = request(1L, 10L,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0),
                                List.of(99L));

                mockStaffAndTheatre(1L, drRam);
                when(equipmentRepository.findAllByIdForUpdate(List.of(99L)))
                                .thenReturn(List.of());

                assertThatThrownBy(() -> schedulingService.schedule(request))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Equipment not found");

                verify(surgeryRepository, never()).save(any(Surgery.class));
        }

        @Test
        void rescheduleKeepsEquipmentAndRejectsSlotWhereEquipmentIsInUse() {
                Equipment cArm = equipment(3L, "C-arm", true);
                Surgery displaced = existingSurgery(61L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0), cArm);

                when(surgeryRepository.findById(61L)).thenReturn(Optional.of(displaced));
                when(theatreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(ot2));
                when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(drRam));
                when(equipmentRepository.findAllByIdForUpdate(List.of(3L)))
                                .thenReturn(List.of(cArm));

                Surgery occupant = existingSurgery(70L, ot1, drPriya,
                                LocalDateTime.of(2030, 1, 11, 8, 0),
                                LocalDateTime.of(2030, 1, 11, 10, 0), cArm);
                occupant.setAssignedNurse(staff(21L, "Nurse B", Role.NURSE));

                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of(occupant));

                assertThatThrownBy(() -> schedulingService.reschedule(61L,
                                new RescheduleRequest(2L,
                                                LocalDateTime.of(2030, 1, 11, 9, 0),
                                                LocalDateTime.of(2030, 1, 11, 11, 0))))
                                .isInstanceOf(SchedulingConflictException.class)
                                .hasMessageContaining("equipment already in use");
        }

        @Test
        void rescheduleKeepsEquipmentWhenMovingToAFreeSlot() {
                Equipment cArm = equipment(3L, "C-arm", true);
                Surgery displaced = existingSurgery(61L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0), cArm);

                when(surgeryRepository.findById(61L)).thenReturn(Optional.of(displaced));
                when(theatreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(ot2));
                when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(drRam));
                when(equipmentRepository.findAllByIdForUpdate(List.of(3L)))
                                .thenReturn(List.of(cArm));
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of());
                when(surgeryRepository.save(any(Surgery.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                SurgeryResponse response = schedulingService.reschedule(61L,
                                new RescheduleRequest(2L,
                                                LocalDateTime.of(2030, 1, 11, 9, 0),
                                                LocalDateTime.of(2030, 1, 11, 11, 0)));

                assertThat(response.equipmentIds()).containsExactly(3L);
                assertThat(response.equipmentNames()).containsExactly("C-arm");
        }

        @Test
        void rejectsEditingCompletedSurgery() {
                Surgery completed = existingSurgery(70L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));
                completed.setStatus(SurgeryStatus.COMPLETED);

                when(surgeryRepository.findByIdForUpdate(70L))
                                .thenReturn(Optional.of(completed));

                assertThatThrownBy(() -> schedulingService.update(70L,
                                request(1L, 10L,
                                                LocalDateTime.of(2030, 1, 10, 12, 0),
                                                LocalDateTime.of(2030, 1, 10, 13, 0))))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("cannot be edited");
        }

        @Test
        void updatesExistingSurgeryAndExcludesItFromConflictCheck() {
                Surgery existing = existingSurgery(70L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                SurgeryRequest request = request(2L, 10L,
                                LocalDateTime.of(2030, 1, 10, 12, 0),
                                LocalDateTime.of(2030, 1, 10, 13, 0));

                when(surgeryRepository.findByIdForUpdate(70L))
                                .thenReturn(Optional.of(existing));
                mockStaffAndTheatre(2L, drRam);
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of(existing));
                when(surgeryRepository.save(any(Surgery.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                SurgeryResponse response = schedulingService.update(70L, request);

                assertThat(response.theatreId()).isEqualTo(2L);
                assertThat(response.startTime()).isEqualTo(request.startTime());
                verify(surgeryRepository).save(existing);
        }

        @Test
        void rejectsEditWhenAnotherSurgeryConflicts() {
                Surgery existing = existingSurgery(70L, ot1, drRam,
                                LocalDateTime.of(2030, 1, 10, 9, 0),
                                LocalDateTime.of(2030, 1, 10, 11, 0));

                Surgery other = existingSurgery(71L, ot2, drRam,
                                LocalDateTime.of(2030, 1, 10, 12, 0),
                                LocalDateTime.of(2030, 1, 10, 14, 0));

                SurgeryRequest request = request(2L, 10L,
                                LocalDateTime.of(2030, 1, 10, 12, 30),
                                LocalDateTime.of(2030, 1, 10, 13, 30));

                when(surgeryRepository.findByIdForUpdate(70L))
                                .thenReturn(Optional.of(existing));
                mockStaffAndTheatre(2L, drRam);
                when(surgeryRepository.findOverlapping(anyList(), any(), any()))
                                .thenReturn(List.of(existing, other));

                assertThatThrownBy(() -> schedulingService.update(70L, request))
                                .isInstanceOf(SchedulingConflictException.class);
        }

        private void mockStaffAndTheatre(Long theatreId, UserAccount surgeon) {
                when(theatreRepository.findByIdForUpdate(theatreId))
                                .thenReturn(Optional.of(theatreId.equals(1L) ? ot1 : ot2));
                when(userAccountRepository.findByIdForUpdate(surgeon.getId()))
                                .thenReturn(Optional.of(surgeon));
                when(userAccountRepository.findByIdForUpdate(20L))
                                .thenReturn(Optional.of(nurse));
        }

        private SurgeryRequest request(Long theatreId, Long surgeonId,
                        LocalDateTime start, LocalDateTime end) {
                return new SurgeryRequest(
                                "Patient X", "Appendectomy", surgeonId, theatreId, 20L,
                                start, end, false, null, null);
        }

        private SurgeryRequest request(Long theatreId, Long surgeonId,
                        LocalDateTime start, LocalDateTime end,
                        List<Long> equipmentIds) {
                return new SurgeryRequest(
                                "Patient X", "Appendectomy", surgeonId, theatreId, 20L,
                                start, end, false, null, equipmentIds);
        }

        private Theatre theatre(Long id, String name) {
                Theatre t = new Theatre(name, "General");
                ReflectionTestUtils.setField(t, "id", id);
                return t;
        }

        private UserAccount staff(Long id, String name, Role role) {
                UserAccount u = new UserAccount(
                                "staff" + id + "@otms.local", "x", name, role);
                ReflectionTestUtils.setField(u, "id", id);
                return u;
        }

        private Surgery existingSurgery(Long id, Theatre theatre,
                        UserAccount surgeon,
                        LocalDateTime start,
                        LocalDateTime end,
                        Equipment... equipment) {
                Surgery s = new Surgery();
                ReflectionTestUtils.setField(s, "id", id);
                s.setPatientName("Existing Patient");
                s.setProcedureName("Existing Procedure");
                s.setLeadSurgeon(surgeon);
                s.setTheatre(theatre);
                s.setAssignedNurse(nurse);
                s.setStartTime(start);
                s.setEndTime(end);
                s.setStatus(SurgeryStatus.SCHEDULED);
                s.getEquipment().addAll(List.of(equipment));
                return s;
        }

        private Equipment equipment(Long id, String name, boolean available) {
                Equipment e = new Equipment(name, "Imaging");
                ReflectionTestUtils.setField(e, "id", id);
                e.setAvailable(available);
                return e;
        }
}
