package com.example.medbook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class ServiceRequest {
    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String serviceName;
    private String description;
    @NotNull(message = "Thời gian khám ước tính là bắt buộc")
    private Integer estimatedDuration;
    @NotNull(message = "Giá dịch vụ là bắt buộc")
    private BigDecimal price;
    @NotNull(message = "Specialty ID(chuyên khoa) là bắt buộc")
    private Integer specialtyId;
    private String imageURL;
}
