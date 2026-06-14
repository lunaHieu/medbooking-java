package com.example.medbook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SystemFeedbackRequest {
    @JsonProperty("Rating")
    private Integer rating;

    @JsonProperty("Comment")
    private String comment;
}
