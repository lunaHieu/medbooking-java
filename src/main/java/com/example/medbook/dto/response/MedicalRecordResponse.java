package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicalRecordResponse {
    @JsonProperty("RecordID")
    private Integer recordId;

    @JsonProperty("AppointmentID")
    private Integer appointmentId;

    @JsonProperty("PatientID")
    private Integer patientId;

    @JsonProperty("DoctorID")
    private Integer doctorId;

    @JsonProperty("Diagnosis")
    private String diagnosis;

    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("DoctorName")
    private String doctorName;

    @JsonProperty("PatientName")
    private String patientName;

    @JsonProperty("SpecialtyName")
    private String specialtyName;

    @JsonProperty("exam_results")
    private List<ExamResultResponse> examResults;

    @JsonProperty("doctor")
    private DoctorProfileResponse doctor;

    @JsonProperty("patient")
    private UserProfileResponse patient;

    @JsonProperty("appointment")
    private AppointmentResponse appointment;
}