package com.example.medbook.repository;

import com.example.medbook.entity.Appointment;
import com.example.medbook.entity.Doctor;
import com.example.medbook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.print.Doc;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByStatus(String status);
    List<Appointment> findByPatient(User patient);
    //Tìm theo Bác sĩ và Thời gian
    List<Appointment> findByDoctorAndStartTimeBetween(Doctor doctor, LocalDateTime startTime, LocalDateTime endTime);
    // Tìm tất cả theo Thời gian (Dùng cho Staff)
    List<Appointment> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    //Tìm theo Bệnh nhân ID và Trạng thái
    List<Appointment> findByPatient_UserIdAndStatus(Integer patientId, String status);
    //Tìm theo Bác sĩ ID và Thời gian
    List<Appointment> findByDoctor_DoctorIdAndStartTimeBetween(Integer doctorId, LocalDateTime start, LocalDateTime end);

    //Tìm lịch hẹn theo khoảng thời gian và trạng thái
    // Dùng để tìm các lịch vào "Ngày mai" và đang "Confirmed"
    List<Appointment> findByStatusAndStartTimeBetween(
            String status,
            LocalDateTime start,
            LocalDateTime end
    );
}
