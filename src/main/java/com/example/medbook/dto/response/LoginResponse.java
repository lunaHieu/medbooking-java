package com.example.medbook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginResponse {
    @JsonProperty("token")
    private String token;

    @JsonProperty("type")
    private String type = "Bearer";

    @JsonProperty("userId")
    private Integer userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("role")
    private String role;

    @JsonProperty("user")
    private UserProfileResponse user;

    public LoginResponse(String token, String type, Integer userId, String username, String role) {
        this.token = token;
        this.type = type;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}
