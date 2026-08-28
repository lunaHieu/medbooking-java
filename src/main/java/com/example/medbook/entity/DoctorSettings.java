package com.example.medbook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "doctor_settings")
@Data
public class DoctorSettings {
    @Id
    @Column(name = "DoctorID")
    private Integer doctorId;

    @Column(name = "NotificationSettings", columnDefinition = "TEXT")
    private String notificationSettings;

    @Column(name = "Preferences", columnDefinition = "TEXT")
    private String preferences;
}
