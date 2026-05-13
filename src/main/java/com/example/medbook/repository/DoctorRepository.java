package com.example.medbook.repository;

import com.example.medbook.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor,Integer> {
    List<Doctor> findBySpecialty_SpecialtyId(Integer specialtyId);
    List<Doctor> findByUser_FirstNameContainingIgnoreCaseOrUser_LastNameContainingIgnoreCaseOrProfileDescriptionContainingIgnoreCase(
            String firstNameQuery, String lastNameQuery, String descriptionQuery);
}
