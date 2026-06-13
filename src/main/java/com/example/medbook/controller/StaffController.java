package com.example.medbook.controller;

import com.example.medbook.dto.request.RegisterRequest;
import com.example.medbook.dto.response.AppointmentResponse;
import com.example.medbook.dto.response.MedicalRecordResponse;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.dto.response.ScheduleResponse;
import com.example.medbook.entity.User;
import com.example.medbook.repository.UserRepository;
import com.example.medbook.service.AppointmentService;
import com.example.medbook.service.AuthService;
import com.example.medbook.service.MedicalRecordService;
import com.example.medbook.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/staff")
@PreAuthorize("hasRole('MEDICAL_STAFF') or hasRole('ADMIN')")
public class StaffController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private MedicalRecordService medicalRecordService;


    //DASHBOARD (BẢNG ĐIỀU KHIỂN)
    @GetMapping("/me")
    public ResponseEntity<MessageResponse> getStaffProfile(){
        return ResponseEntity.ok(new MessageResponse("Xin chào nhân viên y tế!"));
    }

    // Thống kê (Stats Cards)
    @GetMapping({"/dashboard-stats", "/dashboard/stats"})
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(appointmentService.getDailyStats());
    }

    // Lấy danh sách lịch hẹn Pending
    @GetMapping("/pending-appointments")
    public ResponseEntity<?> getPendingAppointments() {
        List<AppointmentResponse> pendingList = appointmentService.getPendingAppointments();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", pendingList);
        response.put("count", pendingList.size());
        return ResponseEntity.ok(response);
    }

    // Duyệt lịch hẹn
    @PatchMapping("/appointments/{id}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable Integer id) {
        appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(Map.of("message", "Duyệt lịch hẹn thành công."));
    }

    // Check-in bệnh nhân
    @PatchMapping("/appointments/{id}/check-in")
    public ResponseEntity<?> checkInAppointment(@PathVariable Integer id) {
        appointmentService.checkInAppointment(id);
        return ResponseEntity.ok(Map.of("message", "Check-in bệnh nhân thành công."));
    }

    // Hủy lịch hẹn kèm lí do
    @PatchMapping("/appointments/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Hủy bởi nhân viên phòng khám");
        appointmentService.cancelAppointmentByStaff(id, reason);
        return ResponseEntity.ok(Map.of("message", "Hủy lịch hẹn thành công."));
    }

    // Staff tạo lịch hẹn trực tiếp (Walk-in)
    @PostMapping(value = "/appointments", consumes = {"multipart/form-data"})
    public ResponseEntity<?> staffCreateAppointment(
            @RequestParam("PatientID") Integer patientId,
            @RequestParam("SlotID") Integer slotId,
            @RequestParam(value = "InitialSymptoms", required = false) String symptoms,
            @RequestParam(value = "Status", required = false) String status) {
        AppointmentResponse response = appointmentService.staffCreateAppointment(slotId, patientId, symptoms, status);
        return ResponseEntity.status(201).body(response);
    }

    // QUẢN LÝ LỊCH HẸN (APPOINTMENT)

    // Xem tất cả lịch hẹn hôm nay (Để gọi điện xác nhận)
    @GetMapping("/appointments/all-today")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointmentsForToday(){
        return ResponseEntity.ok(appointmentService.getAllAppointmentsForToday());
    }

    //Thay đổi trạng thái (Confirm, Check-in, Start, Cancel) - Giữ lại để tương thích ngược
    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Integer id,
            @RequestParam("action") String action) {

        // action có thể là: "confirm", "check-in", "start", "cancel"
        appointmentService.changeStatusByStaff(id, action);
        return ResponseEntity.ok("Cập nhật trạng thái thành công: " + action);
    }

    // 3. QUẢN LÝ BỆNH NHÂN (PATIENT)

    // Tạo bệnh nhân mới tại quầy (Walk-in)
    @PostMapping("/patients")
    public ResponseEntity<?> createWalkInPatient(@RequestBody RegisterRequest request) {
        // Tái sử dụng logic đăng ký của AuthService
        return authService.registerPatient(request);
    }

    //TÌM KIẾM BỆNH NHÂN (Có thể bỏ API này vì mình thực hiện ngoài frontend cũng được)
    // GET /api/staff/patients/search?keyword=0909...
    @GetMapping("/patients/search")
    public ResponseEntity<List<User>> searchPatients(@RequestParam String keyword) {
        // Tìm theo Tên hoặc SĐT hoặc Email
        // (Giả sử bạn đã thêm hàm này vào UserRepository, nếu chưa, dùng findAll tạm)
//         return ResponseEntity.ok(userRepository.searchUsers(keyword));

        // Cách đơn giản hiện tại (trả về tất cả nếu chưa có hàm search):
        return ResponseEntity.ok(userRepository.findAll());
    }

    // XEM CHI TIẾT & LỊCH SỬ KHÁM
    @GetMapping("/patients/{patientId}/history")
    public ResponseEntity<List<MedicalRecordResponse>> getPatientHistory(@PathVariable Integer patientId) {
        // Staff được quyền xem lịch sử để hỗ trợ bác sĩ
        // Chúng ta tái sử dụng logic lấy bệnh án (nhưng cần hàm service cho phép Staff xem)
        // Tạm thời dùng hàm getRecordsByPatientId (lưu ý hàm này hiện tại đang check quyền Doctor)
        // Tốt nhất nên viết thêm 1 hàm getHistoryForStaff trong MedicalRecordService không check currentUser
        return ResponseEntity.ok(medicalRecordService.getHistoryForStaff(patientId));
    }

    //LỊCH LÀM VIỆC BÁC SĨ (SCHEDULE)

    //XEM SLOT RẢNH CỦA BÁC SĨ BẤT KỲ
    // Để Staff tư vấn cho bệnh nhân chọn giờ
    @GetMapping("/doctors/{doctorId}/available-slots")
    public ResponseEntity<List<ScheduleResponse>> getDoctorAvailableSlotsByDate(
            @PathVariable Integer doctorId,
            @RequestParam("target") LocalDate targetDate) {
        // Tái sử dụng hàm public đã có
        return ResponseEntity.ok(scheduleService.getPublicAvailabilityByDate(doctorId, targetDate));
    }

    //KHÓA SLOT ĐỘT XUẤT
    // Khi bác sĩ gọi điện báo bận, Staff khóa hộ
    @PutMapping("/schedules/{slotId}/block")
    public ResponseEntity<MessageResponse> blockDoctorSlot(@PathVariable Integer slotId) {
        // Cần đảm bảo ScheduleService có hàm cancelSlot cho phép Staff gọi (hoặc bỏ check ID)
        // Ở đây gọi tạm hàm cancelSlot (nhưng lưu ý hàm đó đang check ID bác sĩ)
        //cần sửa ScheduleService để cho phép Staff bypass check đó.
        scheduleService.blockSlotByStaff(slotId);
        return ResponseEntity.ok(new MessageResponse("Đã khóa slot thành công."));
    }
}