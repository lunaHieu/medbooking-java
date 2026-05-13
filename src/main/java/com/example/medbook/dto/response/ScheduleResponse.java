package com.example.medbook.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleResponse {
    private Integer slotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}
