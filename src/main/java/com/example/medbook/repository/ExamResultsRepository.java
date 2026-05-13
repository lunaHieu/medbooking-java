package com.example.medbook.repository;

import com.example.medbook.entity.ExamResults;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamResultsRepository extends JpaRepository<ExamResults, Integer> {
}
