package com.example.medbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "medicalrecords")
@Getter
@Setter
@NoArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RecordID")
    private Integer recordId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PatientID", nullable = false)
    private User patient;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DoctorID", nullable = false)
    private Doctor doctor;

    @Column(name = "Diagnosis", columnDefinition = "TEXT")
    private String diagnosis; // Chẩn đoán chính

    @Column(name = "Notes", columnDefinition = "TEXT")
    private String notes; // Ghi chú của Bác sĩ

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    // Liên kết 1-1 với Appointment (1 lịch hẹn chỉ có 1 bệnh án)
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "AppointmentID", unique = true, nullable = false)
    private Appointment appointment;

    //Liên kết 1-N với ExamResults (1 bệnh án có nhiều file xét nghiệm)
    @JsonIgnore
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamResults> examResults;

    // (PatientID và DoctorID được lấy từ Appointment)

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}