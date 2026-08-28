package com.example.medbook.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record DoctorSettingsRequest(
        @NotNull Map<String, Object> notificationSettings,
        @NotNull Map<String, Object> preferences
) {
}
