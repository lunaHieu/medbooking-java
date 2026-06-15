package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    @JsonProperty("AppointmentID")
    private Integer appointmentId;

    @JsonProperty("PatientID")
    private Integer patientId;

    @JsonProperty("DoctorID")
    private Integer doctorId;

    @JsonProperty("SlotID")
    private Integer slotId;

    @JsonProperty("ServiceID")
    private Integer serviceId;

    @JsonProperty("StartTime")
    private LocalDateTime startTime;

    @JsonProperty("EstimatedDuration")
    private Integer estimatedDuration;

    @JsonProperty("InitialSymptoms")
    private String initialSymptoms;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("CancellationReason")
    private String cancellationReason;

    @JsonProperty("file_path")
    private String filePath;

    @JsonProperty("Type")
    private String type = "New";

    // Tên phẳng để tương thích ngược
    @JsonProperty("PatientName")
    private String patientName;

    @JsonProperty("DoctorName")
    private String doctorName;

    @JsonProperty("SpecialtyName")
    private String specialtyName;

    @JsonProperty("ServiceName")
    private String serviceName;

    // Các quan hệ lồng nhau theo đúng frontend model
    @JsonProperty("patient")
    private UserProfileResponse patient;

    @JsonProperty("doctor")
    private DoctorProfileResponse doctor;

    @JsonProperty("service")
    private ServiceResponse service;
}
