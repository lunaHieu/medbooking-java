package com.example.medbook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookAppointmentRequest {
    @NotNull(message = "SlotID (ID khung giờ) là bắt buộc")
    private Integer slotId;

    @NotBlank(message = "Triệu chứng ban đầu không được để trống")
    private String initialSymptoms;

    private Integer serviceId;
}
