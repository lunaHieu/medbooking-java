package com.example.medbook.mapper;

import com.example.medbook.dto.response.ScheduleResponse;
import com.example.medbook.entity.DoctorAvailability;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    ScheduleResponse toScheduleResponse(DoctorAvailability availability);
    List<ScheduleResponse> toScheduleResponseList(List<DoctorAvailability> availabilities);
}
