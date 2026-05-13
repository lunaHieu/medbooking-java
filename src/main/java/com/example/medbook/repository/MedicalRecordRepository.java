package com.example.medbook.repository;

import com.example.medbook.entity.MedicalRecord;
import com.example.medbook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {
    //Kiểm tra xem lịch hẹn này đã có bệnh án chưa
    Optional<MedicalRecord> findByAppointment_AppointmentId(Integer appointmentId);

    List<MedicalRecord> findByAppointment_Patient(User patient);

    //Tìm bệnh án của một bệnh nhân (PatientID) nhưng chỉ do Bác sĩ cụ thể khám
    List<MedicalRecord> findByPatient_UserIdAndDoctor_DoctorId(Integer patientId, Integer doctorId);
}