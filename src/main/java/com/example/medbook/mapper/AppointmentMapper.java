package com.example.medbook.mapper;

import com.example.medbook.dto.response.AppointmentResponse;
import com.example.medbook.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, DoctorMapper.class, ServiceMapper.class})
public interface AppointmentMapper {
    @Mapping(target = "patientName", expression = "java(appointment.getPatient() != null ? ((appointment.getPatient().getLastName() != null ? appointment.getPatient().getLastName() : \"\") + \" \" + (appointment.getPatient().getFirstName() != null ? appointment.getPatient().getFirstName() : \"\")).trim() : \"\")")
    @Mapping(target = "doctorName", expression = "java(appointment.getDoctor() != null && appointment.getDoctor().getUser() != null ? ((appointment.getDoctor().getUser().getLastName() != null ? appointment.getDoctor().getUser().getLastName() : \"\") + \" \" + (appointment.getDoctor().getUser().getFirstName() != null ? appointment.getDoctor().getUser().getFirstName() : \"\")).trim() : \"\")")
    @Mapping(target = "specialtyName", source = "doctor.specialty.specialtyName")
    @Mapping(target = "serviceName", source = "service.serviceName")
    @Mapping(target = "patientId", source = "patient.userId")
    @Mapping(target = "doctorId", source = "doctor.doctorId")
    @Mapping(target = "slotId", source = "slot.slotId")
    @Mapping(target = "serviceId", source = "service.serviceId")
    @Mapping(target = "patient", source = "patient")
    @Mapping(target = "doctor", source = "doctor")
    @Mapping(target = "service", source = "service")
    AppointmentResponse toAppointmentResponse(Appointment appointment);
}
