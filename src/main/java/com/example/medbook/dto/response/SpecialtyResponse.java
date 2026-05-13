package com.example.medbook.dto.response;

import lombok.Data;

@Data
public class SpecialtyResponse {
    private Integer specialtyId;
    private String specialtyName;
    private String description;
    private String imageURL;
}
