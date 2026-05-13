package com.example.medbook.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamResultResponse {
    private Integer resultId;
    private LocalDateTime uploadedAt;
    private String filePath;
    private String fileType;
    private String fileDescription;
}