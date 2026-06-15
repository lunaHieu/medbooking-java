package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackResponse {

    @JsonProperty("FeedbackID")
    private Integer feedbackId;

    @JsonProperty("CreatedAt")
    private LocalDateTime createdAt;

    @JsonProperty("created_at")
    public LocalDateTime getCreatedAtAlternative() {
        return createdAt;
    }

    @JsonProperty("Rating")
    private Integer rating;

    @JsonProperty("Comment")
    private String comment;

    @JsonProperty("PatientName")
    private String patientName;

    @JsonProperty("AppointmentID")
    private Integer appointmentId;

    @JsonProperty("DoctorName")
    private String doctorName;

    @JsonProperty("SpecialtyName")
    private String specialtyName;

    // Các trường phục vụ AdminFeedback
    @JsonProperty("ReviewerName")
    private String reviewerName;

    @JsonProperty("ReviewerAvatar")
    private String reviewerAvatar;

    @JsonProperty("TargetName")
    private String targetName;

    @JsonProperty("Type")
    private String type;
}