package com.example.medbook.service;

import com.example.medbook.dto.request.LoginRequest;
import com.example.medbook.dto.request.RegisterRequest;
import com.example.medbook.dto.response.LoginResponse;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.entity.User;
import com.example.medbook.repository.UserRepository;
import com.example.medbook.security.jwt.JwtUtils;
import com.example.medbook.security.services.UserDetailsImpl;
import com.example.medbook.mapper.UserMapper; // <-- IMPORT MAPPER
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserMapper userMapper; // <-- TIÊM MAPPER

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority())
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        User user = userRepository.findById(userDetails.getId()).orElse(null);
        com.example.medbook.dto.response.UserProfileResponse userProfile = user != null ? userMapper.toUserProfileResponse(user) : null;

        LoginResponse loginResponse = new LoginResponse(jwt,
                "Bearer",
                userDetails.getId(),
                userDetails.getUsername(),
                role);
        loginResponse.setUser(userProfile);

        return ResponseEntity.ok(loginResponse);
    }

    public ResponseEntity<?> registerPatient(RegisterRequest registerRequest) {

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username đã tồn tại!"));
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email đã được sử dụng!"));
        }
        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Số điện thoại đã được sử dụng!"));
        }

        // Tách FullName thành FirstName và LastName nếu chúng bị trống
        String fullName = registerRequest.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            if (registerRequest.getFirstName() == null || registerRequest.getFirstName().trim().isEmpty() ||
                registerRequest.getLastName() == null || registerRequest.getLastName().trim().isEmpty()) {
                
                String[] parts = fullName.trim().split("\\s+");
                if (parts.length > 0) {
                    // Chữ cuối cùng là FirstName
                    String firstName = parts[parts.length - 1];
                    // Các chữ còn lại là LastName
                    StringBuilder lastNameBuilder = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        lastNameBuilder.append(parts[i]).append(" ");
                    }
                    String lastName = lastNameBuilder.toString().trim();
                    
                    registerRequest.setFirstName(firstName);
                    registerRequest.setLastName(lastName.isEmpty() ? "" : lastName);
                } else {
                    registerRequest.setFirstName(fullName);
                    registerRequest.setLastName("");
                }
            }
        }

        // 1. Dùng Mapper để chuyển đổi
        User user = userMapper.toUser(registerRequest);

        // 2. Set các trường logic nghiệp vụ
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole("BenhNhan");
        user.setStatus("Active");

        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email hoặc Số điện thoại đã được sử dụng!"));
        }
        return ResponseEntity.ok(new MessageResponse("Đăng ký tài khoản bệnh nhân thành công!"));
    }
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
