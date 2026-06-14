package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    @JsonProperty("NotificationID")
    private Integer notificationId;

    @JsonProperty("UserID")
    private Integer userId;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Content")
    private String content;

    @JsonProperty("NotificationType")
    private String notificationType;

    @JsonProperty("Channel")
    private String channel;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
