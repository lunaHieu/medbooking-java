package com.example.medbook.repository;

import com.example.medbook.entity.Appointment;
import com.example.medbook.entity.Doctor;
import com.example.medbook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    @Override
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH d.specialty LEFT JOIN FETCH a.service")
    List<Appointment> findAll();

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH d.specialty LEFT JOIN FETCH a.service WHERE a.status = :status")
    List<Appointment> findByStatus(@Param("status") String status);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH d.specialty LEFT JOIN FETCH a.service WHERE a.patient = :patient")
    List<Appointment> findByPatient(@Param("patient") User patient);

    //Tìm theo Bác sĩ và Thời gian
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH d.specialty LEFT JOIN FETCH a.service WHERE a.doctor = :doctor AND a.startTime BETWEEN :startTime AND :endTime")
    List<Appointment> findByDoctorAndStartTimeBetween(
            @Param("doctor") Doctor doctor,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Tìm tất cả theo Thời gian (Dùng cho Staff)
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH d.specialty LEFT JOIN FETCH a.service WHERE a.startTime BETWEEN :startTime AND :endTime")
    List<Appointment> findByStartTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    //Tìm theo Bệnh nhân ID và Trạng thái
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH d.specialty LEFT JOIN FETCH a.service WHERE a.patient.userId = :patientId AND a.status = :status")
    List<Appointment> findByPatient_UserIdAndStatus(
            @Param("patientId") Integer patientId,
            @Param("status") String status);

    //Tìm theo Bác sĩ ID và Thời gian
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH d.specialty LEFT JOIN FETCH a.service WHERE a.doctor.doctorId = :doctorId AND a.startTime BETWEEN :start AND :end")
    List<Appointment> findByDoctor_DoctorIdAndStartTimeBetween(
            @Param("doctorId") Integer doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    //Tìm lịch hẹn theo khoảng thời gian và trạng thái
    // Dùng để tìm các lịch vào "Ngày mai" và đang "Confirmed"
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH d.specialty LEFT JOIN FETCH a.service WHERE a.status = :status AND a.startTime BETWEEN :start AND :end")
    List<Appointment> findByStatusAndStartTimeBetween(
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}

