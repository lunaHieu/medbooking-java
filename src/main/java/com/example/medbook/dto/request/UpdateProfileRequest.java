package com.example.medbook.dto.request;

import lombok.Data;
import java.util.Date;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String gender;
    private String address;
    private String avatarURL;
}