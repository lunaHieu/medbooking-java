package com.example.medbook.dto.response;

import java.util.Map;

public record DoctorSettingsResponse(
        Map<String, Object> notificationSettings,
        Map<String, Object> preferences
) {
}
