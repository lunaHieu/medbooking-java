package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RatingSummaryResponse {
    @JsonProperty("AverageRating")
    private Double averageRating;

    @JsonProperty("TotalReviews")
    private Long totalReviews;

    @JsonProperty("TargetID")
    private Integer targetId;
}