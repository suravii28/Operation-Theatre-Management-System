
package com.otms.service;

import com.otms.dto.NotificationResponse;
import com.otms.model.Notification;
import com.otms.model.NotificationType;
import com.otms.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

        @Mock
        private NotificationRepository notificationRepository;

        @InjectMocks
        private NotificationService notificationService;

        private Notification notification;

        @BeforeEach
        void setUp() {
                notification = new Notification(
                                NotificationType.EMERGENCY_SCHEDULED,
                                "Emergency surgery scheduled",
                                7L);
        }

        @Test
        void recordSavesNotification() {
                when(notificationRepository.save(any(Notification.class)))
                                .thenReturn(notification);

                Notification result = notificationService.record(
                                NotificationType.EMERGENCY_SCHEDULED,
                                7L,
                                "Emergency surgery scheduled");

                assertNotNull(result);
                assertEquals(NotificationType.EMERGENCY_SCHEDULED, result.getType());
                assertEquals("Emergency surgery scheduled", result.getMessage());

                verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        void listReturnsNotificationResponses() {
                when(notificationRepository.findTop50ByOrderByCreatedAtDesc())
                                .thenReturn(List.of(notification));

                List<NotificationResponse> result = notificationService.list();

                assertEquals(1, result.size());
                assertEquals(NotificationType.EMERGENCY_SCHEDULED, result.get(0).type());
                assertEquals("Emergency surgery scheduled", result.get(0).message());
                assertEquals(7L, result.get(0).surgeryId());
                assertFalse(result.get(0).read());
        }

        @Test
        void markReadUpdatesNotification() {
                when(notificationRepository.findById(1L))
                                .thenReturn(Optional.of(notification));
                when(notificationRepository.save(notification))
                                .thenReturn(notification);

                NotificationResponse result = notificationService.markRead(1L);

                assertTrue(result.read());
                verify(notificationRepository).findById(1L);
                verify(notificationRepository).save(notification);
        }

        @Test
        void markReadThrowsExceptionForUnknownId() {
                when(notificationRepository.findById(99L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> notificationService.markRead(99L));

                assertEquals("Notification not found: 99", exception.getMessage());
                verify(notificationRepository, never()).save(any(Notification.class));
        }
}
