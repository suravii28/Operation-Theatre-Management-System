package com.otms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column
    private Long surgeryId;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {
    }

    public Notification(NotificationType type, String message, Long surgeryId) {
        this.type = type;
        this.message = message;
        this.surgeryId = surgeryId;
    }

    public Long getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Long getSurgeryId() {
        return surgeryId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
