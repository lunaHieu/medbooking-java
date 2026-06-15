package com.example.medbook.service;

import com.example.medbook.dto.request.CreateUserRequest;
import com.example.medbook.dto.request.AdminUpdatePatientRequest;
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
import org.springframework.http.HttpStatus;
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
import java.util.List;
import com.example.medbook.dto.response.UserProfileResponse;

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

    @Autowired
    private com.example.medbook.repository.UserRelationRepository userRelationRepository;
    @Autowired
    private com.example.medbook.repository.NotificationRepository notificationRepository;
    @Autowired
    private com.example.medbook.repository.FeedbackRepository feedbackRepository;
    @Autowired
    private com.example.medbook.repository.MedicalRecordRepository medicalRecordRepository;
    @Autowired
    private com.example.medbook.repository.AppointmentRepository appointmentRepository;
    @Autowired
    private com.example.medbook.repository.DoctorAvailabilityRepository doctorAvailabilityRepository;
    @Autowired
    private com.example.medbook.repository.OtpRepository otpRepository;


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

    @Autowired
    private FileStorageService fileStorageService;

    private String saveFile(MultipartFile file, String subDir) {
        return fileStorageService.uploadFile(file);
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

    public List<UserProfileResponse> getAllUsers(String role, String search) {
        List<User> users = userRepository.findAll();
        List<UserProfileResponse> responses = new java.util.ArrayList<>();
        
        for (User u : users) {
            if (role != null && !role.trim().isEmpty()) {
                String expectedRole = role.toUpperCase();
                if (expectedRole.equals("BACSI") || expectedRole.equals("DOCTOR")) {
                    expectedRole = "DOCTOR";
                } else if (expectedRole.equals("NHANVIEN") || expectedRole.equals("STAFF") || expectedRole.equals("MEDICAL_STAFF")) {
                    expectedRole = "MEDICAL_STAFF";
                } else if (expectedRole.equals("BENHNHAN") || expectedRole.equals("PATIENT")) {
                    expectedRole = "PATIENT";
                } else if (expectedRole.equals("QUANTRIYEN") || expectedRole.equals("ADMIN")) {
                    expectedRole = "ADMIN";
                }
                
                if (!expectedRole.equalsIgnoreCase(u.getRole())) {
                    continue;
                }
            }
            
            if (search != null && !search.trim().isEmpty()) {
                String lower = search.toLowerCase();
                String fullName = ((u.getLastName() != null ? u.getLastName() : "") + " " + (u.getFirstName() != null ? u.getFirstName() : "")).toLowerCase();
                boolean matches = (u.getUsername() != null && u.getUsername().toLowerCase().contains(lower)) ||
                                  (u.getPhoneNumber() != null && u.getPhoneNumber().toLowerCase().contains(lower)) ||
                                  (u.getEmail() != null && u.getEmail().toLowerCase().contains(lower)) ||
                                  fullName.contains(lower);
                if (!matches) {
                    continue;
                }
            }
            
            responses.add(userMapper.toUserProfileResponse(u));
        }
        
        return responses;
    }

    public UserProfileResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + id));
        return userMapper.toUserProfileResponse(user);
    }

    @Transactional
    public ResponseEntity<?> adminUpdateUser(
            Integer id, String fullName, String username, String email, String phoneNumber,
            String password, String role, String status, MultipartFile avatar) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + id));

        if (username != null && !username.equalsIgnoreCase(user.getUsername()) && userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username đã tồn tại!"));
        }
        if (phoneNumber != null && !phoneNumber.equalsIgnoreCase(user.getPhoneNumber()) && userRepository.existsByPhoneNumber(phoneNumber)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Số điện thoại đã được sử dụng!"));
        }

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
        
        if (role != null) {
            String upperRole = role.toUpperCase();
            if (upperRole.equals("BACSI") || upperRole.equals("DOCTOR")) {
                user.setRole("DOCTOR");
            } else if (upperRole.equals("NHANVIEN") || upperRole.equals("STAFF") || upperRole.equals("MEDICAL_STAFF")) {
                user.setRole("MEDICAL_STAFF");
            } else if (upperRole.equals("BENHNHAN") || upperRole.equals("PATIENT")) {
                user.setRole("PATIENT");
            } else if (upperRole.equals("QUANTRIYEN") || upperRole.equals("ADMIN")) {
                user.setRole("ADMIN");
            } else {
                user.setRole(upperRole);
            }
        }
        
        if (status != null) user.setStatus(status);

        if (avatar != null && !avatar.isEmpty()) {
            String imagePath = saveFile(avatar, "avatars");
            user.setAvatarURL(imagePath);
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Cập nhật tài khoản thành công!"));
    }

    @Transactional
    public ResponseEntity<?> adminCreateUserMultipart(
            String fullName, String username, String email, String phoneNumber,
            String password, String role, String status, String gender, String dateOfBirthStr) {

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username đã tồn tại!"));
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Số điện thoại đã được sử dụng!"));
        }

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

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(password));
        
        if (role != null) {
            String upperRole = role.toUpperCase();
            if (upperRole.equals("BACSI") || upperRole.equals("DOCTOR")) {
                user.setRole("DOCTOR");
            } else if (upperRole.equals("NHANVIEN") || upperRole.equals("STAFF") || upperRole.equals("MEDICAL_STAFF")) {
                user.setRole("MEDICAL_STAFF");
            } else if (upperRole.equals("BENHNHAN") || upperRole.equals("PATIENT")) {
                user.setRole("PATIENT");
            } else if (upperRole.equals("QUANTRIYEN") || upperRole.equals("ADMIN")) {
                user.setRole("ADMIN");
            } else {
                user.setRole(upperRole);
            }
        } else {
            user.setRole("PATIENT");
        }
        
        user.setStatus(status != null ? status : "Active");
        user.setGender(gender);
        
        if (dateOfBirthStr != null && !dateOfBirthStr.isEmpty()) {
            try {
                user.setDateOfBirth(java.sql.Date.valueOf(dateOfBirthStr));
            } catch (Exception e) {
                // ignore
            }
        }

        userRepository.save(user);

        return new ResponseEntity<>(new MessageResponse("Tạo tài khoản thành công!"), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> adminCreatePatient(
            String fullName, String username, String email, String phoneNumber,
            String password, String status, String gender, String dateOfBirthStr, String address) {

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username đã tồn tại!"));
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Số điện thoại đã được sử dụng!"));
        }
        if (email != null && !email.trim().isEmpty() && userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email đã được sử dụng!"));
        }

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

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("PATIENT");
        user.setStatus(status != null ? status : "Active");
        user.setGender(gender);
        user.setAddress(address);

        if (dateOfBirthStr != null && !dateOfBirthStr.isEmpty()) {
            try {
                user.setDateOfBirth(java.sql.Date.valueOf(dateOfBirthStr));
            } catch (Exception e) {
                // ignore
            }
        }

        userRepository.save(user);

        return new ResponseEntity<>(new MessageResponse("Tạo tài khoản bệnh nhân thành công!"), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> adminUpdatePatient(Integer id, AdminUpdatePatientRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bệnh nhân với ID: " + id));

        if (!"PATIENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.badRequest().body(new MessageResponse("User không phải là bệnh nhân!"));
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equalsIgnoreCase(user.getPhoneNumber()) 
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Số điện thoại đã được sử dụng!"));
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty() 
                && !request.getEmail().equalsIgnoreCase(user.getEmail()) 
                && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email đã được sử dụng!"));
        }

        String fullName = request.getFullName();
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

        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getAddress() != null) user.setAddress(request.getAddress());

        if (request.getStatus() != null) {
            String status = request.getStatus();
            if ("HoatDong".equalsIgnoreCase(status) || "Active".equalsIgnoreCase(status)) {
                user.setStatus("Active");
            } else if ("Khoa".equalsIgnoreCase(status) || "Inactive".equalsIgnoreCase(status) || "Blocked".equalsIgnoreCase(status)) {
                user.setStatus("Blocked");
            } else {
                user.setStatus(status);
            }
        }

        String dobStr = request.getDateOfBirth();
        if (dobStr != null && !dobStr.isEmpty()) {
            try {
                user.setDateOfBirth(java.sql.Date.valueOf(dobStr));
            } catch (Exception e) {
                // ignore
            }
        }

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Cập nhật thông tin bệnh nhân thành công!"));
    }

    @Transactional
    public ResponseEntity<?> adminDeletePatient(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân với ID: " + id));
        if (!"PATIENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.badRequest().body(new MessageResponse("User không phải là bệnh nhân!"));
        }

        doctorAvailabilityRepository.resetSlotsByPatientId(id);
        medicalRecordRepository.deleteByPatientId(id);
        feedbackRepository.deleteByPatientId(id);
        appointmentRepository.deleteByPatientId(id);
        userRelationRepository.deleteByUserOrRelativeUser(id);
        notificationRepository.deleteByUserId(id);

        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            otpRepository.deleteByEmail(user.getEmail());
        }

        userRepository.delete(user);
        return ResponseEntity.ok(new MessageResponse("Xóa bệnh nhân thành công!"));
    }
}
