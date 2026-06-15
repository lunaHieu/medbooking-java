package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceResponse {
    @JsonProperty("ServiceID")
    private Integer serviceId;

    @JsonProperty("SpecialtyID")
    private Integer specialtyId;

    @JsonProperty("ServiceName")
    private String serviceName;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("EstimatedDuration")
    private Integer estimatedDuration;

    @JsonProperty("Price")
    private BigDecimal price;

    @JsonProperty("imageURL")
    private String imageURL;

    @JsonProperty("SpecialtyName")
    private String specialtyName;

    @JsonProperty("FeaturedDoctorName")
    private String featuredDoctorName;

    @JsonProperty("FeaturedDoctorImage")
    private String featuredDoctorImage;
}
