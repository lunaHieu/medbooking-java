package com.example.medbook.controller;

import com.example.medbook.dto.response.*;
import com.example.medbook.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private SpecialtyService specialtyService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ServiceService serviceService;

    //CHUYÊN KHOA (SPECIALTIES)
    @GetMapping("/specialties")
    public ResponseEntity<List<SpecialtyResponse>> getAllSpecialties() {
        List<SpecialtyResponse> specialties = specialtyService.getAllSpecialties();
        return ResponseEntity.ok(specialties);
    }
    //XEM SLOT RẢNH THEO CHUYÊN KHOA
    @GetMapping("/specialties/{specialtyId}/available-slots")
    public ResponseEntity<List<ScheduleResponse>> getSlotsBySpecialty(
            @PathVariable Integer specialtyId,
            @RequestParam(required = false) LocalDate date) {

        List<ScheduleResponse> slots = scheduleService.getAvailableSlotsBySpecialty(specialtyId, date);
        return ResponseEntity.ok(slots);
    }
    @GetMapping("/specialties/{id}")
    public ResponseEntity<SpecialtyResponse> getSpecialtyById(@PathVariable Integer id) {
        SpecialtyResponse specialty = specialtyService.getSpecialtyById(id);
        return ResponseEntity.ok(specialty);
    }

    //BÁC SĨ (DOCTORS)
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorProfileResponse>> getAllDoctors() {
        List<DoctorProfileResponse> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/doctors/{id}")
    public ResponseEntity<DoctorProfileResponse> getDoctorById(@PathVariable Integer id) {
        DoctorProfileResponse doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/doctors/{doctorId}/ratings")
    public ResponseEntity<RatingSummaryResponse> getDoctorRatings(@PathVariable Integer doctorId) {
        RatingSummaryResponse summary = feedbackService.getDoctorRatingSummary(doctorId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/doctors/{doctorId}/feedbacks")
    public ResponseEntity<List<FeedbackResponse>> getDoctorFeedbacks(@PathVariable Integer doctorId) {
        List<FeedbackResponse> feedbacks = feedbackService.getDetailedDoctorFeedback(doctorId);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/doctors/search")
    public ResponseEntity<List<DoctorProfileResponse>> getDoctors(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer specialtyId) {
        List<DoctorProfileResponse> doctors = doctorService.searchDoctor(query, specialtyId);
        return ResponseEntity.ok(doctors);
    }


    // LỊCH RẢNH CỦA BÁC SĨ (PUBLIC SLOTS)

    //Lấy lịch rảnh theo khoảng ngày (Mặc định 1 ngày nếu không truyền days)
    @GetMapping("/doctors/{doctorId}/slots")
    public ResponseEntity<List<ScheduleResponse>> getPublicAvailableSlots(
            @PathVariable Integer doctorId,
            @RequestParam(required = false) Integer days) {

        List<ScheduleResponse> slots = scheduleService.getPublicAvailableSlots(doctorId, days);
        return ResponseEntity.ok(slots);
    }

    //Lấy lịch rảnh theo ngày cụ thể
    @GetMapping("/doctors/{doctorId}/slots/date")
    public ResponseEntity<List<ScheduleResponse>> getDoctorAvailabilityByDate(
            @PathVariable Integer doctorId,
            @RequestParam("target") LocalDate targetDate) {

        List<ScheduleResponse> slots = scheduleService.getPublicAvailabilityByDate(doctorId, targetDate);
        return ResponseEntity.ok(slots);
    }

    //DỊCH VỤ (SERVICES)
    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        List<ServiceResponse> services = serviceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Integer id) {
        ServiceResponse service = serviceService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/services/{serviceId}/doctors")
    public ResponseEntity<List<DoctorProfileResponse>> getDoctorsByService(@PathVariable Integer serviceId) {
        List<DoctorProfileResponse> doctors = doctorService.getDoctorsByService(serviceId);
        return ResponseEntity.ok(doctors);
    }
    //FEEDBACK NỔI BẬT
    @GetMapping("/feedbacks/featured")
    public ResponseEntity<List<FeedbackResponse>> getFeaturedFeedbacks() {
        List<FeedbackResponse> feedbacks = feedbackService.getFeaturedFeedbacks();
        return ResponseEntity.ok(feedbacks);
    }
}