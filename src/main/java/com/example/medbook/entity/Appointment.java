package com.example.medbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AppointmentID")
    private Integer appointmentId;

    @Column(name = "StartTime")
    private LocalDateTime startTime;

    @Column(name = "EstimatedDuration")
    private Integer estimatedDuration;

    @Column(name = "InitialSymptoms")
    private String initialSymptoms;

    @Column(name = "Status")
    private String status;

    @Column(name = "CancellationReason")
    private String cancellationReason;

    @Column(name = "CreatedAt", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PatientID", nullable = false)
    private User patient;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DoctorID",nullable = false)
    private Doctor doctor;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SlotID")
    private DoctorAvailability slot;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceID")
    private Service service;
    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
    }
}
