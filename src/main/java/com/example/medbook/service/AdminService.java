package com.example.medbook.service;

import com.example.medbook.dto.request.CreateUserRequest;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.entity.Doctor;
import com.example.medbook.entity.Specialty;
import com.example.medbook.entity.User;
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.repository.DoctorRepository;
import com.example.medbook.repository.SpecialtyRepository;
import com.example.medbook.repository.UserRepository;
import com.example.medbook.mapper.UserMapper; // <-- IMPORT MAPPER
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AdminService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserMapper userMapper;

    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    SpecialtyRepository specialtyRepository;

    private static final Set<String> ALLOWED_ROLES = Set.of("DOCTOR", "MEDICAL_STAFF", "ADMIN");
    @Transactional
    public ResponseEntity<?> createUser(CreateUserRequest createUserRequest) {

        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username đã tồn tại!"));
        }
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email đã được sử dụng!"));
        }
        if (userRepository.existsByPhoneNumber(createUserRequest.getPhoneNumber())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Số điện thoại đã được sử dụng!"));
        }

        String requestedRole = createUserRequest.getRole().toUpperCase();
        if (!ALLOWED_ROLES.contains(requestedRole)) {
            throw new IllegalArgumentException("Vai trò không hợp lệ");
        }

        // 1. Dùng Mapper để chuyển đổi (Mapper đã tự map 'role')
        User user = userMapper.toUser(createUserRequest);

        // 2. Set các trường logic nghiệp vụ
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setStatus("Active");
        user.setRole(requestedRole); // Đảm bảo role là chữ HOA

        User savedUser = userRepository.save(user);
        if(requestedRole.equals("DOCTOR")){
            Integer specialtyId = createUserRequest.getSpecialtyId();
            if(specialtyId == null){
                throw new IllegalArgumentException("Phải cung cấp SpecialtyID khi tạo Bác sĩ!");
            }
            Specialty specialty = specialtyRepository.findById(specialtyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Chuyên khoa với ID: " + specialtyId));
            Doctor doctorProfile = new Doctor();
            doctorProfile.setUser(savedUser);
            doctorProfile.setSpecialty(specialty);

            doctorProfile.setDegree(createUserRequest.getDegree());
            doctorProfile.setYearsOfExperience(createUserRequest.getYearsOfExperience());
            doctorProfile.setProfileDescription(createUserRequest.getProfileDescription());
            doctorProfile.setImageURL(createUserRequest.getImageURL());

            doctorRepository.save(doctorProfile);
        }
        return ResponseEntity.ok(new MessageResponse("Tạo tài khoản " + requestedRole + " thành công!"));
    }
}
