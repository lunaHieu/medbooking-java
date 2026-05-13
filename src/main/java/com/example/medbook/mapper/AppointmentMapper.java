package com.example.medbook.mapper;

import com.example.medbook.dto.response.AppointmentResponse;
import com.example.medbook.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    @Mapping(target = "patientName", ignore = true)
    @Mapping(target = "doctorName", ignore = true)
    @Mapping(target = "specialtyName", source = "doctor.specialty.specialtyName")
    AppointmentResponse toAppointmentResponse(Appointment appointment);
}
