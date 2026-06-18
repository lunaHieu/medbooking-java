package com.example.medbook.repository;

import com.example.medbook.entity.MedicalRecord;
import com.example.medbook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {

    @Modifying
    @Query("DELETE FROM MedicalRecord mr WHERE mr.patient.userId = :patientId")
    void deleteByPatientId(@Param("patientId") Integer patientId);
    //Kiểm tra xem lịch hẹn này đã có bệnh án chưa
    @Query("SELECT m FROM MedicalRecord m WHERE m.appointment.appointmentId = :appointmentId")
    Optional<MedicalRecord> findByAppointment_AppointmentId(@Param("appointmentId") Integer appointmentId);

    @Query("SELECT m FROM MedicalRecord m WHERE m.appointment.patient = :patient")
    List<MedicalRecord> findByAppointment_Patient(@Param("patient") User patient);

    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.userId = :patientId AND m.doctor.doctorId = :doctorId")
    List<MedicalRecord> findByPatient_UserIdAndDoctor_DoctorId(@Param("patientId") Integer patientId, @Param("doctorId") Integer doctorId);

    @Query("SELECT m FROM MedicalRecord m WHERE m.doctor.doctorId = :doctorId")
    List<MedicalRecord> findByDoctor_DoctorId(@Param("doctorId") Integer doctorId);
}