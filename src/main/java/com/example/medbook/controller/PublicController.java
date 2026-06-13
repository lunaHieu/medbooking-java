package com.example.medbook.controller;

import com.example.medbook.dto.response.*;
import com.example.medbook.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = {"https://medbooking-client-flax.vercel.app", "http://localhost:3000"})
public class PublicController {

    @Autowired
    private SpecialtyService specialtyService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private ScheduleService scheduleService;

    // CHUYEN KHOA (SPECIALTIES)
    @GetMapping("/public/specialties")
    public ResponseEntity<List<SpecialtyResponse>> getAllSpecialties(@RequestParam(required = false, name = "search") String search) {
        List<SpecialtyResponse> specialties = specialtyService.getAllSpecialties();
        if (search != null && !search.trim().isEmpty()) {
            String lowerSearch = search.toLowerCase();
            List<SpecialtyResponse> filtered = new java.util.ArrayList<>();
            for (SpecialtyResponse s : specialties) {
                if (s.getSpecialtyName() != null && s.getSpecialtyName().toLowerCase().contains(lowerSearch)) {
                    filtered.add(s);
                }
            }
            specialties = filtered;
        }
        return ResponseEntity.ok(specialties);
    }

    // XEM SLOT RANH THEO CHUYEN KHOA
    @GetMapping("/public/specialties/{specialtyId}/available-slots")
    public ResponseEntity<List<ScheduleResponse>> getSlotsBySpecialty(
            @PathVariable Integer specialtyId,
            @RequestParam(required = false) LocalDate date) {

        List<ScheduleResponse> slots = scheduleService.getAvailableSlotsBySpecialty(specialtyId, date);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/public/specialties/{id}")
    public ResponseEntity<SpecialtyResponse> getSpecialtyById(@PathVariable Integer id) {
        SpecialtyResponse specialty = specialtyService.getSpecialtyById(id);
        return ResponseEntity.ok(specialty);
    }

    // BAC SI (DOCTORS)
    @GetMapping("/public/doctors")
    public ResponseEntity<List<DoctorProfileResponse>> getDoctors(
            @RequestParam(required = false, name = "search") String search,
            @RequestParam(required = false, name = "query") String query,
            @RequestParam(required = false, name = "specialty_id") Integer specialtyId,
            @RequestParam(required = false, name = "specialtyId") Integer specialtyIdCamel) {
        String finalSearch = (search != null) ? search : query;
        Integer finalSpecialtyId = (specialtyId != null) ? specialtyId : specialtyIdCamel;

        if ((finalSearch != null && !finalSearch.trim().isEmpty()) || finalSpecialtyId != null) {
            List<DoctorProfileResponse> doctors = doctorService.searchDoctor(finalSearch, finalSpecialtyId);
            return ResponseEntity.ok(doctors);
        }
        List<DoctorProfileResponse> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/public/doctors/{id}")
    public ResponseEntity<DoctorProfileResponse> getDoctorById(@PathVariable Integer id) {
        DoctorProfileResponse doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/public/doctors/{doctorId}/ratings")
    public ResponseEntity<RatingSummaryResponse> getDoctorRatings(@PathVariable Integer doctorId) {
        RatingSummaryResponse summary = feedbackService.getDoctorRatingSummary(doctorId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/public/doctors/{doctorId}/feedbacks")
    public ResponseEntity<List<FeedbackResponse>> getDoctorFeedbacks(@PathVariable Integer doctorId) {
        List<FeedbackResponse> feedbacks = feedbackService.getDetailedDoctorFeedback(doctorId);
        return ResponseEntity.ok(feedbacks);
    }

    // LICH RANH CUA BAC SI (PUBLIC SLOTS)

    // Lay lich ranh theo khoang ngay (Mac dinh 1 ngay neu khong truyen days)
    @GetMapping("/public/doctors/{doctorId}/slots")
    public ResponseEntity<List<ScheduleResponse>> getPublicAvailableSlots(
            @PathVariable Integer doctorId,
            @RequestParam(required = false) Integer days) {

        List<ScheduleResponse> slots = scheduleService.getPublicAvailableSlots(doctorId, days);
        return ResponseEntity.ok(slots);
    }

    // Lay lich ranh theo ngay cu the
    @GetMapping("/public/doctors/{doctorId}/slots/date")
    public ResponseEntity<List<ScheduleResponse>> getDoctorAvailabilityByDate(
            @PathVariable Integer doctorId,
            @RequestParam("target") LocalDate targetDate) {

        List<ScheduleResponse> slots = scheduleService.getPublicAvailabilityByDate(doctorId, targetDate);
        return ResponseEntity.ok(slots);
    }

    // FEEDBACK NOI BAT
    @GetMapping("/public/feedbacks/featured")
    public ResponseEntity<List<FeedbackResponse>> getFeaturedFeedbacks() {
        List<FeedbackResponse> feedbacks = feedbackService.getFeaturedFeedbacks();
        return ResponseEntity.ok(feedbacks);
    }
}
