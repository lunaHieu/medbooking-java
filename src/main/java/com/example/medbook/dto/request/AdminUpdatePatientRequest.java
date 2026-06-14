package com.example.medbook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdminUpdatePatientRequest {
    @JsonProperty("FullName")
    private String fullName;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("DateOfBirth")
    private String dateOfBirth;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("Status")
    private String status;
}
