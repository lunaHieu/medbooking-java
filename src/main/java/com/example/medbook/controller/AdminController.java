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

import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/admin")
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

    //CRUD CHUYÊN KHOA (Specialty Management)

    // API: POST /api/admin/specialties (Tạo mới)
    @PostMapping("/specialties")
    public ResponseEntity<SpecialtyResponse> createSpecialty(@Valid @RequestBody SpecialtyRequest request) {
        SpecialtyResponse newSpecialty = specialtyService.createSpecialty(request);
        return new ResponseEntity<>(newSpecialty, HttpStatus.CREATED);
    }

    // API: PUT /api/admin/specialties/{id} (Cập nhật)
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
    @PostMapping("/services")
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse serviceResponse = serviceService.createService(request);
        return new ResponseEntity<>(serviceResponse, HttpStatus.CREATED);
    }

    // API: PUT /api/admin/services/{id} (Cập nhật)
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
}