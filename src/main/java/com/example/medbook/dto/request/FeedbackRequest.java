package com.example.medbook.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotNull(message = "Appointment ID là bắt buộc")
    private Integer appointmentId;

    @NotNull(message = "Đánh giá sao là bắt buộc")
    @Min(value = 1, message = "Điểm đánh giá phải từ 1 sao trở lên")
    @Max(value = 5, message = "Điểm đánh giá không được quá 5 sao")
    private Integer rating;

    private String comment;

    private String targetType;
}