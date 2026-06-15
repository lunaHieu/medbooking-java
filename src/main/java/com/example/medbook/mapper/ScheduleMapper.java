package com.example.medbook.mapper;

import com.example.medbook.dto.response.ScheduleResponse;
import com.example.medbook.entity.DoctorAvailability;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AppointmentMapper.class})
public interface ScheduleMapper {
    @Mapping(target = "doctorId", source = "doctor.doctorId")
    @Mapping(target = "appointment", source = "appointment")
    ScheduleResponse toScheduleResponse(DoctorAvailability availability);
    List<ScheduleResponse> toScheduleResponseList(List<DoctorAvailability> availabilities);
}
