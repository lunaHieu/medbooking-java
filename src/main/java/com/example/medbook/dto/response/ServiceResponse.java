package com.example.medbook.dto.response;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ServiceResponse {
    private Integer serviceId;
    private String serviceName;
    private String description;
    private Integer estimatedDuration;
    private BigDecimal price;
    private String imageURL;

    private String specialtyName;
    private String featuredDoctorName;
    private String featuredDoctorImage;
}
