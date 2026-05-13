package com.example.medbook.dto.response;

import lombok.Data;
import java.util.Date;

@Data
public class UserProfileResponse {
    private Integer userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String gender;
    private String address;
    private String avatarURL;
    private String role;
}