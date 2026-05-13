package com.example.medbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "examresults")
@Getter
@Setter
@NoArgsConstructor
public class ExamResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ResultID")
    private Integer resultId;

    @Column(name = "FilePath", nullable = false)
    private String filePath;

    @Column(name = "FileType")
    private String fileType;

    @Column(name = "FileDescription")
    private String fileDescription;

    @Column(name = "UploadedAt")
    private LocalDateTime uploadedAt;

    // Liên kết N-1: Nhiều kết quả thuộc về 1 MedicalRecord
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RecordID", nullable = false)
    private MedicalRecord medicalRecord;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}