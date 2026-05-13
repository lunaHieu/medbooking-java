package com.example.medbook.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicalRecordRequest {

    @NotNull(message = "Appointment ID là bắt buộc để tạo hồ sơ bệnh án")
    private Integer appointmentId;

    @NotNull(message = "Chẩn đoán không được để trống")
    private String diagnosis;

    private String notes;
}