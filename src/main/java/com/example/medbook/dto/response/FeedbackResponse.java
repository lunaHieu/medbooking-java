package com.example.medbook.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackResponse {

    private Integer feedbackId;
    private LocalDateTime createdAt;
    private Integer rating;
    private String comment;
    private String patientName;
    private Integer appointmentId;
    private String doctorName;
    private String specialtyName;
}