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
import com.example.medbook.mapper.UserMapper;
import com.example.medbook.dto.response.DoctorProfileResponse;
import com.example.medbook.mapper.DoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

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

    @Autowired
    DoctorMapper doctorMapper;

    private String saveFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("src/main/resources/uploads").resolve(subDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return subDir + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file. Error: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ResponseEntity<?> createDoctor(
            String fullName, String username, String email, String phoneNumber,
            String password, Integer specialtyId, String degree, Integer yearsOfExperience,
            String profileDescription, String status, MultipartFile file) {

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username đã tồn tại!"));
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email đã được sử dụng!"));
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Số điện thoại đã được sử dụng!"));
        }

        // Split FullName
        String firstName = "";
        String lastName = "";
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length > 0) {
                firstName = parts[parts.length - 1];
                if (parts.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        sb.append(parts[i]).append(" ");
                    }
                    lastName = sb.toString().trim();
                } else {
                    lastName = firstName;
                }
            }
        }

        String imagePath = null;
        if (file != null && !file.isEmpty()) {
            imagePath = saveFile(file, "uploads/doctors");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("DOCTOR");
        user.setStatus(status != null ? status : "Active");
        user.setAvatarURL(imagePath);
        User savedUser = userRepository.save(user);

        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Chuyên khoa với ID: " + specialtyId));

        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setSpecialty(specialty);
        doctor.setDegree(degree);
        doctor.setYearsOfExperience(yearsOfExperience != null ? yearsOfExperience : 0);
        doctor.setProfileDescription(profileDescription);
        doctor.setImageURL(imagePath);
        doctorRepository.save(doctor);

        return ResponseEntity.ok(new MessageResponse("Tạo hồ sơ Bác sĩ thành công!"));
    }

    @Transactional
    public ResponseEntity<?> updateDoctor(
            Integer id, String fullName, String username, String email, String phoneNumber,
            String password, Integer specialtyId, String degree, Integer yearsOfExperience,
            String profileDescription, String status, MultipartFile file) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bác sĩ với ID: " + id));
        User user = doctor.getUser();

        // Check unique fields
        if (username != null && !username.equalsIgnoreCase(user.getUsername()) && userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username đã tồn tại!"));
        }
        if (email != null && !email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email đã được sử dụng!"));
        }
        if (phoneNumber != null && !phoneNumber.equalsIgnoreCase(user.getPhoneNumber()) && userRepository.existsByPhoneNumber(phoneNumber)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Số điện thoại đã được sử dụng!"));
        }

        // Split FullName
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length > 0) {
                String firstName = parts[parts.length - 1];
                String lastName = "";
                if (parts.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        sb.append(parts[i]).append(" ");
                    }
                    lastName = sb.toString().trim();
                } else {
                    lastName = firstName;
                }
                user.setFirstName(firstName);
                user.setLastName(lastName);
            }
        }

        if (username != null) user.setUsername(username);
        if (email != null) user.setEmail(email);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
        if (password != null && !password.trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }
        if (status != null) user.setStatus(status);

        if (file != null && !file.isEmpty()) {
            String imagePath = saveFile(file, "uploads/doctors");
            user.setAvatarURL(imagePath);
            doctor.setImageURL(imagePath);
        }

        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Chuyên khoa với ID: " + specialtyId));
        doctor.setSpecialty(specialty);
        if (degree != null) doctor.setDegree(degree);
        if (yearsOfExperience != null) doctor.setYearsOfExperience(yearsOfExperience);
        if (profileDescription != null) doctor.setProfileDescription(profileDescription);

        userRepository.save(user);
        doctorRepository.save(doctor);

        return ResponseEntity.ok(new MessageResponse("Cập nhật hồ sơ Bác sĩ thành công!"));
    }

    @Transactional
    public ResponseEntity<?> uploadDoctorImage(Integer id, MultipartFile file) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bác sĩ với ID: " + id));
        
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("file tải lên không được để trống."));
        }
        
        String imagePath = saveFile(file, "uploads/doctors");
        doctor.setImageURL(imagePath);
        doctor.getUser().setAvatarURL(imagePath);
        
        doctorRepository.save(doctor);
        userRepository.save(doctor.getUser());
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Tải ảnh lên thành công!");
        response.put("image_url", "/storage/" + imagePath);
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<?> deleteDoctor(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bác sĩ với ID: " + id));
        if (!"DOCTOR".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Không tìm thấy Bác sĩ với ID này."));
        }
        
        doctorRepository.deleteById(id);
        userRepository.delete(user);
        
        return ResponseEntity.ok(new MessageResponse("Xóa Bác sĩ thành công."));
    }
}
