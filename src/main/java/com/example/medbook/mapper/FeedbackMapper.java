package com.example.medbook.mapper;

import com.example.medbook.dto.response.FeedbackResponse;
import com.example.medbook.entity.Feedbacks;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    @Mapping(target = "patientName", ignore = true)
    @Mapping(target = "doctorName", ignore = true)


    @Mapping(target = "specialtyName", source = "appointment.doctor.specialty.specialtyName")

    @Mapping(target = "appointmentId", source = "appointment.appointmentId")
    FeedbackResponse toFeedbackResponse(Feedbacks feedback);
}