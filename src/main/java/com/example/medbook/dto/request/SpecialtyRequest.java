package com.example.medbook.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SpecialtyRequest {
    @NotBlank(message = "Tên chuyên khoa không được để trống!")
    private String specialtyName;

    private String description;

    private String imageURL;
}
