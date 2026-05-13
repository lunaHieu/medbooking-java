package com.example.medbook.dto.response;

import lombok.Data;

@Data
public class DoctorProfileResponse {
    private Integer userId;
    private Integer doctorId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String avatarURL;
    private String phoneNumber;
    private String degree;
    private String email;
    private Integer yearsOfExperience;
    private String profileDescription;
    private String imageURL;

    private String specialtyName;
}
