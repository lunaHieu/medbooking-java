package com.example.medbook.controller;

import com.example.medbook.dto.request.BookAppointmentRequest;
import com.example.medbook.dto.request.FeedbackRequest;
import com.example.medbook.dto.request.UpdateProfileRequest;
import com.example.medbook.dto.response.*;
import com.example.medbook.entity.Appointment;
import com.example.medbook.security.services.UserDetailsImpl;
import com.example.medbook.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private UserService userService;
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(userService.getMyProfile(currentUser));
    }
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        UserProfileResponse response = userService.updateMyProfile(currentUser, request);
        return ResponseEntity.ok(response);
    }
    //Bệnh nhân đặt lịch hẹn
    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        AppointmentResponse newAppointment = appointmentService.bookAppointment(request, currentUser);
        return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);

    }

    @GetMapping("/appointments/me")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(currentUser));

    }
    @PutMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<String> cancelAppointment(@PathVariable Integer appointmentId) {

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        appointmentService.cancelPatientAppointment(appointmentId, currentUsername);

        return ResponseEntity.ok("Hủy Lịch hẹn ID " + appointmentId + " thành công.");
    }
    @GetMapping("/medical-records/me")
    public ResponseEntity<List<MedicalRecordResponse>> getMyMedicalRecords(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<MedicalRecordResponse> records = medicalRecordService.getMyMedicalRecords(currentUser);
        return new ResponseEntity<>(records, HttpStatus.OK);
    }
    @PostMapping("/feedback")
    public ResponseEntity<MessageResponse> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        return feedbackService.submitFeedback(request, currentUser);
    }
    //Xem lịch của bất kì bác sĩ nào
    @GetMapping("/doctors/{doctorId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getDoctorSchedulesByDate(
            @PathVariable Integer doctorId,
            @RequestParam("targetDate") LocalDate targetDate) { // BẮT BUỘC phải có ngày

        // Tái sử dụng hàm logic đã viết trong ScheduleService
        List<ScheduleResponse> schedules = scheduleService.getDoctorSchedulesByDate(doctorId, targetDate);
        return ResponseEntity.ok(schedules);
    }
    @GetMapping("/previous-doctors")
    public ResponseEntity<List<DoctorProfileResponse>> getPreviousDoctors(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        return ResponseEntity.ok(appointmentService.getPreviousDoctorsForPatient(currentUser));
    }
}
