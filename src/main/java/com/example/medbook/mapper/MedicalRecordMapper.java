package com.example.medbook.mapper;

import com.example.medbook.dto.response.ExamResultResponse;
import com.example.medbook.dto.response.MedicalRecordResponse;
import com.example.medbook.entity.ExamResults;
import com.example.medbook.entity.MedicalRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, DoctorMapper.class, AppointmentMapper.class})
public interface MedicalRecordMapper {

    MedicalRecordMapper INSTANCE = Mappers.getMapper(MedicalRecordMapper.class);

    @Mapping(target = "recordId", source = "medicalRecord.recordId")
    ExamResultResponse toExamResultResponse(ExamResults examResults);
    List<ExamResultResponse> toExamResultResponseList(List<ExamResults> examResults);

    @Mapping(target = "appointmentId", source = "appointment.appointmentId")
    @Mapping(target = "patientId", source = "patient.userId")
    @Mapping(target = "doctorId", source = "doctor.doctorId")
    @Mapping(target = "patientName", expression = "java(record.getPatient() != null ? (record.getPatient().getLastName() + \" \" + record.getPatient().getFirstName()).trim() : \"\")")
    @Mapping(target = "doctorName", expression = "java(record.getDoctor() != null && record.getDoctor().getUser() != null ? (record.getDoctor().getUser().getLastName() + \" \" + record.getDoctor().getUser().getFirstName()).trim() : \"\")")
    @Mapping(target = "specialtyName", source = "appointment.doctor.specialty.specialtyName")
    @Mapping(target = "patient", source = "patient")
    @Mapping(target = "doctor", source = "doctor")
    @Mapping(target = "appointment", source = "appointment")
    MedicalRecordResponse toMedicalRecordResponse(MedicalRecord record);

    List<MedicalRecordResponse> toMedicalRecordResponseList(List<MedicalRecord> records);
}