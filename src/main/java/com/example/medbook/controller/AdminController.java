package com.example.medbook.controller;

import com.example.medbook.dto.request.CreateUserRequest;
import com.example.medbook.dto.request.ScheduleRequest;
import com.example.medbook.dto.request.ServiceRequest;
import com.example.medbook.dto.request.SpecialtyRequest;
import com.example.medbook.dto.response.*;
import com.example.medbook.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService adminService; // Quản lý User/Doctor/Staff

    @Autowired
    SpecialtyService specialtyService; // Quản lý Chuyên khoa

    @Autowired
    private ScheduleService scheduleService; // Quản lý Lịch Bác sĩ

    @Autowired
    private ServiceService serviceService; // Quản lý Dịch vụ

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private AppointmentService appointmentService;
    //QUẢN LÝ NGƯỜI DÙNG VÀ PROFILE (User/Doctor/Staff Creation)
    @PostMapping("/users")
    public ResponseEntity<?> createNewUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        return adminService.createUser(createUserRequest);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(adminService.getAllUsers(role, search));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PostMapping(value = "/users/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> adminUpdateUser(
            @PathVariable Integer id,
            @RequestParam("FullName") String fullName,
            @RequestParam("Username") String username,
            @RequestParam(value = "Email", required = false) String email,
            @RequestParam("PhoneNumber") String phoneNumber,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam("Role") String role,
            @RequestParam("Status") String status,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        return adminService.adminUpdateUser(id, fullName, username, email, phoneNumber, password, role, status, avatar);
    }

    @PostMapping(value = "/users", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> adminCreateUserMultipart(
            @RequestParam("FullName") String fullName,
            @RequestParam("Username") String username,
            @RequestParam(value = "Email", required = false) String email,
            @RequestParam("PhoneNumber") String phoneNumber,
            @RequestParam("password") String password,
            @RequestParam("Role") String role,
            @RequestParam("Status") String status,
            @RequestParam(value = "Gender", required = false) String gender,
            @RequestParam(value = "DateOfBirth", required = false) String dateOfBirth) {
        return adminService.adminCreateUserMultipart(fullName, username, email, phoneNumber, password, role, status, gender, dateOfBirth);
    }

    //CRUD CHUYÊN KHOA (Specialty Management)

    private String saveFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String fileName = java.util.UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("src/main/resources/uploads").resolve(subDir).toAbsolutePath().normalize();
            java.nio.file.Files.createDirectories(uploadPath);
            java.nio.file.Path targetLocation = uploadPath.resolve(fileName);
            java.nio.file.Files.copy(file.getInputStream(), targetLocation, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return subDir + "/" + fileName;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not store file. Error: " + e.getMessage(), e);
        }
    }

    // API: POST /api/admin/specialties (Tạo mới)
    @PostMapping(value = "/specialties", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpecialtyResponse> createSpecialtyMultipart(
            @RequestParam("SpecialtyName") String specialtyName,
            @RequestParam(value = "Description", required = false) String description,
            @RequestParam(value = "imageURL", required = false) MultipartFile file) {
        
        String imagePath = null;
        if (file != null && !file.isEmpty()) {
            imagePath = saveFile(file, "uploads/specialties");
        }
        
        SpecialtyRequest request = new SpecialtyRequest();
        request.setSpecialtyName(specialtyName);
        request.setDescription(description);
        request.setImageURL(imagePath);
        
        SpecialtyResponse newSpecialty = specialtyService.createSpecialty(request);
        return new ResponseEntity<>(newSpecialty, HttpStatus.CREATED);
    }

    @PostMapping("/specialties")
    public ResponseEntity<SpecialtyResponse> createSpecialty(@Valid @RequestBody SpecialtyRequest request) {
        SpecialtyResponse newSpecialty = specialtyService.createSpecialty(request);
        return new ResponseEntity<>(newSpecialty, HttpStatus.CREATED);
    }

    // API: POST /api/admin/specialties/{id} (Cập nhật)
    @PostMapping(value = "/specialties/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpecialtyResponse> updateSpecialtyMultipart(
            @PathVariable Integer id,
            @RequestParam("SpecialtyName") String specialtyName,
            @RequestParam(value = "Description", required = false) String description,
            @RequestParam(value = "imageURL", required = false) MultipartFile file) {
        
        SpecialtyResponse existing = specialtyService.getSpecialtyById(id);
        String imagePath = existing.getImageURL();
        
        if (file != null && !file.isEmpty()) {
            imagePath = saveFile(file, "uploads/specialties");
        }
        
        SpecialtyRequest request = new SpecialtyRequest();
        request.setSpecialtyName(specialtyName);
        request.setDescription(description);
        request.setImageURL(imagePath);
        
        SpecialtyResponse updatedSpecialty = specialtyService.updateSpecialty(id, request);
        return ResponseEntity.ok(updatedSpecialty);
    }

    @PutMapping("/specialties/{id}")
    public ResponseEntity<SpecialtyResponse> updateSpecialty(@PathVariable Integer id, @Valid @RequestBody SpecialtyRequest request) {
        SpecialtyResponse updatedSpecialty = specialtyService.updateSpecialty(id, request);
        return ResponseEntity.ok(updatedSpecialty);
    }

    // API: DELETE /api/admin/specialties/{id} (Xóa)
    @DeleteMapping("/specialties/{id}")
    public ResponseEntity<MessageResponse> deleteSpecialty(@PathVariable Integer id) {
        return specialtyService.deleteSpecialty(id);
    }

    //CRUD DỊCH VỤ (Service Management)

    // API: POST /api/admin/services (Tạo mới)
    @PostMapping(value = "/services", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> createServiceMultipart(
            @RequestParam("ServiceName") String serviceName,
            @RequestParam("SpecialtyID") Integer specialtyId,
            @RequestParam("Price") java.math.BigDecimal price,
            @RequestParam("EstimatedDuration") Integer estimatedDuration,
            @RequestParam(value = "Description", required = false) String description,
            @RequestParam(value = "imageURL", required = false) MultipartFile file) {
        
        String imagePath = null;
        if (file != null && !file.isEmpty()) {
            imagePath = saveFile(file, "uploads/services");
        }
        
        ServiceRequest request = new ServiceRequest();
        request.setServiceName(serviceName);
        request.setSpecialtyId(specialtyId);
        request.setPrice(price);
        request.setEstimatedDuration(estimatedDuration);
        request.setDescription(description);
        request.setImageURL(imagePath);
        
        ServiceResponse newService = serviceService.createService(request);
        return new ResponseEntity<>(newService, HttpStatus.CREATED);
    }

    @PostMapping("/services")
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse serviceResponse = serviceService.createService(request);
        return new ResponseEntity<>(serviceResponse, HttpStatus.CREATED);
    }

    // API: POST /api/admin/services/{id} (Cập nhật)
    @PostMapping(value = "/services/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> updateServiceMultipart(
            @PathVariable Integer id,
            @RequestParam("ServiceName") String serviceName,
            @RequestParam("SpecialtyID") Integer specialtyId,
            @RequestParam("Price") java.math.BigDecimal price,
            @RequestParam("EstimatedDuration") Integer estimatedDuration,
            @RequestParam(value = "Description", required = false) String description,
            @RequestParam(value = "imageURL", required = false) MultipartFile file) {
        
        ServiceResponse existing = serviceService.getServiceById(id);
        String imagePath = existing.getImageURL();
        
        if (file != null && !file.isEmpty()) {
            imagePath = saveFile(file, "uploads/services");
        }
        
        ServiceRequest request = new ServiceRequest();
        request.setServiceName(serviceName);
        request.setSpecialtyId(specialtyId);
        request.setPrice(price);
        request.setEstimatedDuration(estimatedDuration);
        request.setDescription(description);
        request.setImageURL(imagePath);
        
        ServiceResponse updatedService = serviceService.updateService(id, request);
        return ResponseEntity.ok(updatedService);
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<ServiceResponse> updateService(@PathVariable Integer id, @Valid @RequestBody ServiceRequest request) {
        ServiceResponse updatedService = serviceService.updateService(id, request);
        return ResponseEntity.ok(updatedService);
    }

    // API: DELETE /api/admin/services/{id} (Xóa)
    @DeleteMapping("/services/{id}")
    public ResponseEntity<MessageResponse> deleteService(@PathVariable Integer id) {
        return serviceService.deleteService(id);
    }

    // LỊCH LÀM VIỆC (Schedule Viewing)

    // API: GET /api/admin/doctors/{doctorId}/schedules (Xem lịch làm việc của Bác sĩ X)
    @GetMapping("/doctors/{doctorId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getDoctorSchedulesByDate(
            @PathVariable Integer doctorId,
            @RequestParam("targetDate") LocalDate targetDate) { // TargetDate bắt buộc

        List<ScheduleResponse> schedules = scheduleService.getDoctorSchedulesByDate(doctorId, targetDate);
        return ResponseEntity.ok(schedules);
    }
    //QUẢN LÝ PHẢN HỒI (FEEDBACK MANAGEMENT)
    @GetMapping("/feedbacks")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedbacks(
            @RequestParam(required = false) String type) {

        // Gọi hàm service
        return ResponseEntity.ok(feedbackService.getFeedbacksForAdmin(type));
    }
    //TẠO LỊCH CHO BÁC SĨ
    @PostMapping("/doctors/{doctorId}/schedules")
    public ResponseEntity<MessageResponse> createScheduleForDoctor(
            @PathVariable Integer doctorId,
            @Valid @RequestBody ScheduleRequest request) {

        return scheduleService.createScheduleForDoctor(doctorId, request);
    }
    //KHÓA/HỦY SLOT
    @PutMapping("/schedules/{slotId}/cancel")
    public ResponseEntity<MessageResponse> cancelSlotByAdmin(@PathVariable Integer slotId) {

        scheduleService.cancelSlotByAdmin(slotId);
        return ResponseEntity.ok(new MessageResponse("Admin đã khóa slot thành công."));
    }

    // XEM TẤT CẢ LỊCH HẸN (Cho Admin)
    @GetMapping("/all-appointments")
    public ResponseEntity<Map<String, Object>> getAllAppointments() {
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", appointments);
        return ResponseEntity.ok(response);
    }

    // ==================== QUẢN LÝ BÁC SĨ (DOCTOR CRUD) ====================

    @PostMapping(value = "/doctors", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createDoctor(
            @RequestParam("FullName") String fullName,
            @RequestParam("Username") String username,
            @RequestParam("Email") String email,
            @RequestParam("PhoneNumber") String phoneNumber,
            @RequestParam("password") String password,
            @RequestParam("SpecialtyID") Integer specialtyId,
            @RequestParam("Degree") String degree,
            @RequestParam("YearsOfExperience") Integer yearsOfExperience,
            @RequestParam(value = "ProfileDescription", required = false) String profileDescription,
            @RequestParam(value = "Status", required = false) String status,
            @RequestParam(value = "imageURL", required = false) MultipartFile file) {
        
        return adminService.createDoctor(fullName, username, email, phoneNumber, password, specialtyId, degree, yearsOfExperience, profileDescription, status, file);
    }

    @PostMapping(value = "/doctors/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDoctor(
            @PathVariable Integer id,
            @RequestParam("FullName") String fullName,
            @RequestParam("Username") String username,
            @RequestParam("Email") String email,
            @RequestParam("PhoneNumber") String phoneNumber,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam("SpecialtyID") Integer specialtyId,
            @RequestParam("Degree") String degree,
            @RequestParam("YearsOfExperience") Integer yearsOfExperience,
            @RequestParam(value = "ProfileDescription", required = false) String profileDescription,
            @RequestParam(value = "Status", required = false) String status,
            @RequestParam(value = "imageURL", required = false) MultipartFile file) {
        
        return adminService.updateDoctor(id, fullName, username, email, phoneNumber, password, specialtyId, degree, yearsOfExperience, profileDescription, status, file);
    }

    @PostMapping(value = "/doctors/{id}/upload-image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDoctorImage(
            @PathVariable Integer id,
            @RequestParam("imageURL") MultipartFile file) {
        
        return adminService.uploadDoctorImage(id, file);
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Integer id) {
        return adminService.deleteDoctor(id);
    }
}