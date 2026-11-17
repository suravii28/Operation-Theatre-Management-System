package com.otms.service;

import com.otms.dto.NotificationResponse;
import com.otms.model.Notification;
import com.otms.model.NotificationType;
import com.otms.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification record(NotificationType type, Long surgeryId, String message) {
        return notificationRepository.save(new Notification(type, message, surgeryId));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list() {
        return notificationRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getMessage(), n.getSurgeryId(),
                n.getCreatedAt(), n.isRead());
    }
}
