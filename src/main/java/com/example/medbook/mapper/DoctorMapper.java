package com.example.medbook.mapper;

import com.example.medbook.dto.response.DoctorProfileResponse;
import com.example.medbook.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, SpecialtyMapper.class})
public interface DoctorMapper {
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "avatarURL", source = "user.avatarURL")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "specialtyName", source = "specialty.specialtyName")
    @Mapping(target = "specialtyId", source = "specialty.specialtyId")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "fullName", expression = "java(doctor.getUser() != null ? (doctor.getUser().getLastName() + \" \" + doctor.getUser().getFirstName()).trim() : \"\")")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "specialty", source = "specialty")
    DoctorProfileResponse toDoctorProfileResponse(Doctor doctor);

    List<DoctorProfileResponse> toDoctorProfileResponseList(List<Doctor> doctors);
}
