package com.example.medbook.mapper;

import com.example.medbook.dto.response.ExamResultResponse;
import com.example.medbook.dto.response.MedicalRecordResponse;
import com.example.medbook.entity.ExamResults;
import com.example.medbook.entity.MedicalRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    MedicalRecordMapper INSTANCE = Mappers.getMapper(MedicalRecordMapper.class);

    ExamResultResponse toExamResultResponse(ExamResults examResults);
    List<ExamResultResponse> toExamResultResponseList(List<ExamResults> examResults);

    @Mapping(target = "appointmentId", source = "appointment.appointmentId")
    @Mapping(target = "patientName", ignore = true)
    @Mapping(target = "doctorName", ignore = true)
    @Mapping(target = "specialtyName", source = "appointment.doctor.specialty.specialtyName")
    MedicalRecordResponse toMedicalRecordResponse(MedicalRecord record);

    List<MedicalRecordResponse> toMedicalRecordResponseList(List<MedicalRecord> records);
}