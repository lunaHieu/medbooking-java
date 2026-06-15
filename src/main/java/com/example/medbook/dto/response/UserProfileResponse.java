package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;

@Data
public class UserProfileResponse {
    @JsonProperty("UserID")
    private Integer userId;

    @JsonProperty("Username")
    private String username;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("FullName")
    private String fullName;

    @JsonProperty("DateOfBirth")
    private Date dateOfBirth;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("avatar_url")
    private String avatarURL;

    @JsonProperty("Role")
    private String role;

    @JsonProperty("Status")
    private String status;
}