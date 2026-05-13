package com.example.medbook.repository;

import com.example.medbook.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Integer> {
    Optional<Specialty> findBySpecialtyNameContainingIgnoreCase(String specialtyName);
}
