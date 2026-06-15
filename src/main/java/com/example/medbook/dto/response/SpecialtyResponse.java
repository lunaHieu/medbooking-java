package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SpecialtyResponse {
    @JsonProperty("SpecialtyID")
    private Integer specialtyId;

    @JsonProperty("SpecialtyName")
    private String specialtyName;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("imageURL")
    private String imageURL;
}
