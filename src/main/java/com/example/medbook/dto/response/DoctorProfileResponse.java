package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DoctorProfileResponse {
    @JsonProperty("UserID")
    private Integer userId;

    @JsonProperty("DoctorID")
    private Integer doctorId;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("FullName")
    private String fullName;

    @JsonProperty("avatar_url")
    private String avatarURL;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("Degree")
    private String degree;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("YearsOfExperience")
    private Integer yearsOfExperience;

    @JsonProperty("ProfileDescription")
    private String profileDescription;

    @JsonProperty("imageURL")
    private String imageURL;

    @JsonProperty("SpecialtyID")
    private Integer specialtyId;

    @JsonProperty("SpecialtyName")
    private String specialtyName;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("user")
    private UserProfileResponse user;

    @JsonProperty("specialty")
    private SpecialtyResponse specialty;
}
