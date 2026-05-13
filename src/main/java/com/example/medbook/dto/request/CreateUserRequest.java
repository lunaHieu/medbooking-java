package com.example.medbook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {
    @NotBlank
    @Size(min = 3,max = 50)
    private String username;

    @NotBlank
    @Size(min = 6,max = 50)
    private String password;

    @NotBlank
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phoneNumber;

    private String address;
    private String avatarURL;
    @NotBlank
    private String role;

    private Integer specialtyId;
    private String degree;
    private Integer yearsOfExperience;
    private String profileDescription;
    private String imageURL;

}
