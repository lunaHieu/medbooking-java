package com.example.medbook.dto.response;

import lombok.Data;

@Data
public class RatingSummaryResponse {
    private Double averageRating;
    private Long totalReviews;
    private Integer targetId;
}