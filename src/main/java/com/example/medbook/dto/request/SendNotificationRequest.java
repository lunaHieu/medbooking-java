package com.example.medbook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SendNotificationRequest {
    @JsonProperty("Title")
    private String title;

    @JsonProperty("Content")
    private String content;

    @JsonProperty("TargetGroup")
    private String targetGroup; // "all", "patients", "doctors", "staff"

    @JsonProperty("Channel")
    private String channel; // "in_app", "email"
}
