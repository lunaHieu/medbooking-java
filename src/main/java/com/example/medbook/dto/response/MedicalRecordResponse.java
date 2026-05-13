package com.example.medbook.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicalRecordResponse {

    private Integer recordId;
    private LocalDateTime createdAt;
    private String diagnosis;
    private String notes;

    private Integer appointmentId;
    private String doctorName;
    private String patientName;
    private String specialtyName;

    private List<ExamResultResponse> examResults;
}