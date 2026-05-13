package com.example.medbook.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    private Integer appointmentId;
    private LocalDateTime startTime;
    private Integer estimatedDuration;
    private String initialSymptoms;
    private String status;

    private String patientName;
    private String doctorName;
    private String specialtyName;
    private String serviceName;
}
