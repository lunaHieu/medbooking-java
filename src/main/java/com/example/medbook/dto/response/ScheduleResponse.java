package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleResponse {
    @JsonProperty("SlotID")
    private Integer slotId;

    @JsonProperty("DoctorID")
    private Integer doctorId;

    @JsonProperty("StartTime")
    private LocalDateTime startTime;

    @JsonProperty("EndTime")
    private LocalDateTime endTime;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("appointment")
    private AppointmentResponse appointment;
}
