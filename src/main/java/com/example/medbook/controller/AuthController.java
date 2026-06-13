package com.example.medbook.controller;

import com.example.medbook.dto.request.LoginRequest;
import com.example.medbook.dto.request.RegisterRequest;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.registerPatient(registerRequest);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        authService.logout();
        return ResponseEntity.ok(new MessageResponse("Đăng xuất thành công!"));
    }
}
