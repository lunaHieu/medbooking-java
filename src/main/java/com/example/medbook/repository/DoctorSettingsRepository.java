package com.example.medbook.repository;

import com.example.medbook.entity.DoctorSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorSettingsRepository extends JpaRepository<DoctorSettings, Integer> {
}
