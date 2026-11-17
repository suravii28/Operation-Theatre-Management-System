package com.otms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "surgeries")
public class Surgery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String procedureName;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private UserAccount leadSurgeon;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Theatre theatre;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SurgeryStatus status = SurgeryStatus.SCHEDULED;

    @Column(nullable = false)
    private boolean emergency = false;

    private String notes;

    public Surgery() {
    }

    public Long getId() {
        return id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public UserAccount getLeadSurgeon() {
        return leadSurgeon;
    }

    public void setLeadSurgeon(UserAccount leadSurgeon) {
        this.leadSurgeon = leadSurgeon;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public SurgeryStatus getStatus() {
        return status;
    }

    public void setStatus(SurgeryStatus status) {
        this.status = status;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
