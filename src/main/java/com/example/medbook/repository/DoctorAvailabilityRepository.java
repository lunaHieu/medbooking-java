package com.example.medbook.repository;

import com.example.medbook.entity.Doctor;
import com.example.medbook.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability,Integer> {

    void deleteAllByDoctor_DoctorId(Integer doctorId);
    @Modifying
    @Query("UPDATE DoctorAvailability da SET da.status = 'Available' WHERE da.slotId IN (SELECT a.slot.slotId FROM Appointment a WHERE a.patient.userId = :patientId AND a.slot IS NOT NULL)")
    void resetSlotsByPatientId(@Param("patientId") Integer patientId);
    List<DoctorAvailability> findByDoctor(Doctor doctor);
    List<DoctorAvailability> findByDoctor_DoctorIdAndStatusAndStartTimeBetween(Integer doctorId, String status, LocalDateTime start, LocalDateTime end);
    List<DoctorAvailability> findByDoctor_DoctorIdAndStartTimeBetween(Integer doctorId, LocalDateTime start, LocalDateTime end);
    List<DoctorAvailability> findByDoctor_Specialty_SpecialtyIdAndStatusAndStartTimeBetween(
            Integer specialtyId,
            String status,
            LocalDateTime start,
            LocalDateTime end
    );
}
