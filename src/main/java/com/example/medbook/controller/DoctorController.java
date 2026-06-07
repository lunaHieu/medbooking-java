package com.example.medbook.controller;

import com.example.medbook.dto.request.MedicalRecordRequest;
import com.example.medbook.dto.request.ScheduleRequest;
import com.example.medbook.dto.response.*;
import com.example.medbook.security.services.UserDetailsImpl;
import com.example.medbook.service.AppointmentService;
import com.example.medbook.service.MedicalRecordService;
import com.example.medbook.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private MedicalRecordService medicalRecordService;

    // DASHBOARD & PROFILE
    @GetMapping("/me")
    public ResponseEntity<MessageResponse> getDoctorProfile(){
        return ResponseEntity.ok(new MessageResponse("Xin chào bác sĩ!"));
    }

    // API Thống kê
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Long>> getStats(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(appointmentService.getDoctorDailyStats(currentUser.getId()));
    }

    // 2. QUẢN LÝ LỊCH LÀM VIỆC (SCHEDULE)

    // Tạo lịch làm việc
    @PostMapping("/schedules")
    public ResponseEntity<MessageResponse> createSchedule(
            @Valid @RequestBody ScheduleRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ){
        return scheduleService.createDoctorSchedule(request, currentUser);
    }

    // Xem lịch làm việc của tôi (theo ngày hoặc tất cả)
    @GetMapping("/schedules/me")
    public ResponseEntity<List<ScheduleResponse>> getMySchedules(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(required = false) LocalDate targetDate) {

        List<ScheduleResponse> schedules = scheduleService.getMySchedules(currentUser, targetDate);
        return ResponseEntity.ok(schedules);
    }

    // Khóa slot đột xuất
    @PutMapping("/schedules/{slotId}/cancel")
    public ResponseEntity<MessageResponse> cancelSlot(
            @PathVariable Integer slotId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        scheduleService.cancelSlot(slotId, currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("Đã khóa slot thành công."));
    }

    // QUẢN LÝ LỊCH HẸN (APPOINTMENT)

    // Xem lịch hẹn hôm nay
    @GetMapping("/appointments/my-day")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointmentsForToday(@AuthenticationPrincipal UserDetailsImpl currentUser){
        return ResponseEntity.ok(appointmentService.getDoctorAppointmentsForToday(currentUser));
    }

    // Xem lịch hẹn theo khoảng thời gian (from -> to)
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getDoctorSchedule(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam("from") LocalDate from,
            @RequestParam("to") LocalDate to) {

        return ResponseEntity.ok(appointmentService.getDoctorSchedule(currentUser, from, to));
    }

    // Bắt đầu khám (CheckedIn -> InProgress)
    @PutMapping("/appointments/{id}/start")
    public ResponseEntity<MessageResponse> startExamination(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        appointmentService.startDoctorExamination(id, currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("Đã bắt đầu ca khám!"));
    }

    // Hoàn tất khám (InProgress -> Completed)
    // (LƯU Ý: Tôi đã xóa hàm trùng lặp, chỉ giữ lại hàm chuẩn này)
    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<MessageResponse> completeAppointment(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        appointmentService.completeAppointment(id, currentUser.getUsername());
        return ResponseEntity.ok(new MessageResponse("Ca khám đã hoàn tất."));
    }

    //QUẢN LÝ BỆNH ÁN (MEDICAL RECORD)

    // Tạo bệnh án
    @PostMapping("/medical-records")
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ){
        MedicalRecordResponse record = medicalRecordService.createMedicalRecord(request,currentUser);
        return new ResponseEntity<>(record, HttpStatus.CREATED);
    }

    // Upload kết quả xét nghiệm
    @PostMapping(value = "/medical-records/{recordId}/results", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExamResultResponse> uploadExamResults(
            @PathVariable Integer recordId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ){
        if(file.isEmpty()){
            throw new IllegalArgumentException("file tải lên không được để trống.");
        }
        ExamResultResponse result = medicalRecordService.uploadExamResults(recordId, description, file, currentUser);
        return ResponseEntity.ok(result);
    }

    // Xem lịch sử bệnh nhân (Chỉ xem những gì mình đã khám)
    @GetMapping("/patients/{patientId}/medical-records")
    public ResponseEntity<List<MedicalRecordResponse>> getPatientHistory(
            @PathVariable Integer patientId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(medicalRecordService.getRecordsByPatientId(patientId, currentUser));
    }
}