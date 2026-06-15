package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamResultResponse {
    @JsonProperty("ResultID")
    private Integer resultId;

    @JsonProperty("RecordID")
    private Integer recordId;

    @JsonProperty("FilePath")
    private String filePath;

    @JsonProperty("FileType")
    private String fileType;

    @JsonProperty("FileDescription")
    private String fileDescription;

    @JsonProperty("created_at")
    private LocalDateTime uploadedAt;
}