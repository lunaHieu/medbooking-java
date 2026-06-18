package com.example.medbook.service;

import com.example.medbook.dto.request.BookAppointmentRequest;
import com.example.medbook.dto.response.AppointmentResponse;
import com.example.medbook.dto.response.DoctorProfileResponse;
import com.example.medbook.entity.Appointment;
import com.example.medbook.entity.Doctor;
import com.example.medbook.entity.DoctorAvailability;
import com.example.medbook.entity.User;
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.mapper.AppointmentMapper;
import com.example.medbook.repository.*;
import com.example.medbook.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service // <-- Đảm bảo @Service chỉ có 1 lần ở đây
public class AppointmentService {

    // --- HẰNG SỐ (Constants) TRẠNG THÁI ---
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_CONFIRMED = "Confirmed";
    private static final String STATUS_CHECKED_IN = "CheckedIn";
    private static final String STATUS_IN_PROGRESS = "InProgress";
    private static final String STATUS_COMPLETED = "Completed";
    private static final String STATUS_CANCELLED = "Cancelled";
    private static final String STATUS_AVAILABLE = "Available"; // Cho bảng DoctorAvailability
    private static final String STATUS_BOOKED = "Booked";
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private FileStorageService fileStorageService;
    //ĐẶT LỊCH (Book)
    @Transactional
    public AppointmentResponse bookAppointment(BookAppointmentRequest request, MultipartFile file, UserDetailsImpl currentUser) {
        Integer patientId = currentUser.getId();
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bệnh nhân với UserID: " + patientId));

        Integer slotId = request.getSlotId();
        DoctorAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ rảnh với SlotID: " + slotId));

        // Kiểm tra availability slot: phải là "Available"
        if (!STATUS_AVAILABLE.equalsIgnoreCase(slot.getStatus())){
            throw new IllegalArgumentException("Lỗi: Khung giờ này đã được đặt hoặc không còn khả dụng.");
        }

        // Khóa slot lại
        slot.setStatus(STATUS_CONFIRMED); // Đặt lịch sẽ tự động chuyển slot sang 'Confirmed' hoặc 'Booked'
        availabilityRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(slot.getDoctor());
        appointment.setSlot(slot);
        appointment.setStatus(STATUS_PENDING); // Lịch hẹn bắt đầu ở trạng thái "Pending"
        appointment.setStartTime(slot.getStartTime());


        if (request.getServiceId() != null) {
            com.example.medbook.entity.Service service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Dịch vụ ID: " + request.getServiceId()));

            appointment.setService(service); // Lưu Service vào Appointment

            //Nếu Service có quy định thời gian khám, dùng nó. Nếu không, dùng thời gian của Slot)
            if (service.getEstimatedDuration() != null) {
                appointment.setEstimatedDuration(service.getEstimatedDuration());
            } else {
                long duration = java.time.Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
                appointment.setEstimatedDuration((int) duration);
            }
        } else {
            // Nếu không chọn dịch vụ, mặc định lấy thời gian của slot (30 phút)
            long duration = java.time.Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
            appointment.setEstimatedDuration((int) duration);
        }

        appointment.setInitialSymptoms(request.getInitialSymptoms());

        if (file != null && !file.isEmpty()) {
            try {
                String fileUrl = fileStorageService.uploadFile(file);
                System.out.println("Uploaded appointment attachment to Cloudinary: " + fileUrl);
            } catch (Exception e) {
                System.err.println("Lỗi upload file khi đặt lịch: " + e.getMessage());
            }
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(savedAppointment);
    }

    // XEM LỊCH SỬ (Cho Bệnh nhân)
    public List<AppointmentResponse> getMyAppointments(UserDetailsImpl currentUser) {
        User patient = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        List<Appointment> list = appointmentRepository.findByPatient(patient);

        return list.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    //XEM LỊCH HÔM NAY (Cho Bác sĩ)
    public List<AppointmentResponse> getDoctorAppointmentsForToday(UserDetailsImpl currentUser) {
        java.util.Optional<Doctor> doctorOpt = doctorRepository.findById(currentUser.getId());
        if (doctorOpt.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        Doctor doctor = doctorOpt.get();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Appointment> list = appointmentRepository.findByDoctorAndStartTimeBetween(doctor, startOfDay, endOfDay);

        return list.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    //XEM TOÀN BỘ LỊCH HÔM NAY (Cho Nhân viên y tế)
    public List<AppointmentResponse> getAllAppointmentsForToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Appointment> list = appointmentRepository.findByStartTimeBetween(startOfDay, endOfDay);

        return list.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    //Cho Bác sĩ xem lịch theo khoảng ngày
    public List<AppointmentResponse> getDoctorSchedule(UserDetailsImpl currentUser, LocalDate fromDate, LocalDate toDate) {
        java.util.Optional<Doctor> doctorOpt = doctorRepository.findById(currentUser.getId());
        if (doctorOpt.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        Doctor doctor = doctorOpt.get();

        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(LocalTime.MAX);

        List<Appointment> list = appointmentRepository.findByDoctorAndStartTimeBetween(doctor, start, end);

        return list.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    //Xác nhận lịch hẹn (Staff)
    @Transactional
    public void confirmAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(()->new ResourceNotFoundException("Không tìm thấy Lịch hẹn với ID: " + appointmentId));

        if(!STATUS_PENDING.equalsIgnoreCase(appointment.getStatus())){ // Dùng Hằng số
            throw new IllegalStateException("Lịch hẹn chỉ có thể được xác nhận khi ở trạng thái 'Pending'.");
        }
        appointment.setStatus(STATUS_CONFIRMED); // Dùng Hằng số
        appointmentRepository.save(appointment);
    }

    public List<AppointmentResponse> getPendingAppointments() {
        List<Appointment> list = appointmentRepository.findByStatus(STATUS_PENDING);
        return list.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void checkInAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Lịch hẹn với ID: " + appointmentId));
        if (!STATUS_CONFIRMED.equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Lịch hẹn phải ở trạng thái Confirmed trước khi check-in.");
        }
        appointment.setStatus(STATUS_CHECKED_IN);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelAppointmentByStaff(Integer appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Lịch hẹn với ID: " + appointmentId));
        if (STATUS_COMPLETED.equalsIgnoreCase(appointment.getStatus()) || STATUS_CANCELLED.equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Lịch hẹn đã hoàn tất hoặc đã hủy.");
        }
        appointment.setStatus(STATUS_CANCELLED);
        appointment.setCancellationReason(reason);
        appointmentRepository.save(appointment);

        if (appointment.getSlot() != null) {
            DoctorAvailability slot = appointment.getSlot();
            slot.setStatus(STATUS_AVAILABLE);
            availabilityRepository.save(slot);
        }
    }

    @Transactional
    public AppointmentResponse staffCreateAppointment(Integer slotId, Integer patientId, String symptoms, String status) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bệnh nhân với UserID: " + patientId));

        DoctorAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ với SlotID: " + slotId));

        if (!STATUS_AVAILABLE.equalsIgnoreCase(slot.getStatus())) {
            throw new IllegalArgumentException("Khung giờ này không khả dụng.");
        }

        slot.setStatus(STATUS_BOOKED);
        availabilityRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(slot.getDoctor());
        appointment.setSlot(slot);
        appointment.setStatus(status != null ? status : STATUS_PENDING);
        appointment.setStartTime(slot.getStartTime());
        appointment.setInitialSymptoms(symptoms);
        
        long duration = java.time.Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
        appointment.setEstimatedDuration((int) duration);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(savedAppointment);
    }

    // 6. Hoàn tất lịch hẹn (Doctor)
    @Transactional
    public void completeAppointment(Integer appointmentId, String doctorUsername) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Lịch hẹn với ID: " + appointmentId));

        if (!appointment.getDoctor().getUser().getUsername().equals(doctorUsername)) {
            throw new AccessDeniedException("Bạn không có quyền hoàn tất lịch hẹn của Bác sĩ khác");
        }
        String status = appointment.getStatus();
        if (!STATUS_CONFIRMED.equalsIgnoreCase(status)
                && !STATUS_CHECKED_IN.equalsIgnoreCase(status)
                && !STATUS_IN_PROGRESS.equalsIgnoreCase(status)) {
            throw new IllegalStateException("Lịch hẹn chỉ có thể được hoàn tất khi ở trạng thái 'Confirmed', 'CheckedIn' hoặc 'InProgress'.");
        }
        appointment.setStatus(STATUS_COMPLETED); // Dùng Hằng số
        appointmentRepository.save(appointment);
    }

    // 7. Hủy lịch hẹn (Bệnh nhân/Staff)
    @Transactional
    public void cancelPatientAppointment(Integer appointmentId, String patientUsername) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Lịch hẹn với ID: " + appointmentId));

        if (STATUS_COMPLETED.equals(appointment.getStatus()) || STATUS_CANCELLED.equals(appointment.getStatus())) { // Dùng Hằng số
            throw new IllegalStateException("Lịch hẹn ID " + appointmentId + " không thể bị hủy.");
        }

        // Cần kiểm tra quyền sở hữu (đã được sửa ở lần trước)

        appointment.setStatus(STATUS_CANCELLED); // Dùng Hằng số
        appointmentRepository.save(appointment);

        // Giải phóng Slot (RẤT QUAN TRỌNG)
        DoctorAvailability slot = appointment.getSlot();
        slot.setStatus(STATUS_AVAILABLE); // Dùng Hằng số
        availabilityRepository.save(slot);
    }
    // 1. XỬ LÝ CHECK-IN & START (Gộp logic chuyển trạng thái)
    @Transactional
    public void changeStatusByStaff(Integer appointmentId, String action) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn ID: " + appointmentId));

        switch (action.toLowerCase()) {
            case "confirm":
                if (!"Pending".equalsIgnoreCase(app.getStatus()))
                    throw new IllegalStateException("Chỉ xác nhận được lịch hẹn đang Pending.");
                app.setStatus("Confirmed");
                break;
            case "check-in":
                if (!"Confirmed".equalsIgnoreCase(app.getStatus()))
                    throw new IllegalStateException("Bệnh nhân phải được xác nhận (Confirmed) trước khi Check-in.");
                app.setStatus("CheckedIn"); // Trạng thái mới: Bệnh nhân đã đến
                break;
            case "start":
                if (!"CheckedIn".equalsIgnoreCase(app.getStatus()))
                    throw new IllegalStateException("Bệnh nhân chưa Check-in.");
                app.setStatus("InProgress"); // Trạng thái mới: Đang khám
                break;
            case "cancel":
                app.setStatus("Cancelled");
                // Giải phóng slot
                if (app.getSlot() != null) {
                    app.getSlot().setStatus("Available");
                    availabilityRepository.save(app.getSlot());
                }
                break;
            default:
                throw new IllegalArgumentException("Hành động không hợp lệ: " + action);
        }
        appointmentRepository.save(app);
    }

    //THỐNG KÊ CHO STAFF (Stats Cards)
    public Map<String, Long> getDailyStats() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        // Dùng repo đếm trực tiếp (Tối ưu Performance hơn là tải list về rồi đếm)
        // (Giả sử bạn đã thêm các hàm count... trong Repository hoặc dùng findAll rồi stream filter)
        // Cách nhanh nhất không cần sửa Repo nhiều:
        List<Appointment> todayApps = appointmentRepository.findByStartTimeBetween(start, end);

        Map<String, Long> stats = new HashMap<>();
        stats.put("total_today", (long) todayApps.size());
        stats.put("checked_in", todayApps.stream().filter(a -> "CheckedIn".equalsIgnoreCase(a.getStatus())).count());
        stats.put("in_progress", todayApps.stream().filter(a -> "InProgress".equalsIgnoreCase(a.getStatus())).count());

        // Đếm số bác sĩ có lịch hôm nay
        long activeDoctors = todayApps.stream().map(a -> a.getDoctor().getDoctorId()).distinct().count();
        stats.put("doctors_working", activeDoctors);

        return stats;
    }
    //Thống kê cho bác sĩ
    public Map<String, Long> getDoctorDailyStats(Integer doctorId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        // Tìm lịch của riêng bác sĩ này trong hôm nay
        List<Appointment> todayApps = appointmentRepository.findByDoctor_DoctorIdAndStartTimeBetween(doctorId, start, end);

        Map<String, Long> stats = new HashMap<>();
        stats.put("total_today", (long) todayApps.size());
        stats.put("completed", todayApps.stream().filter(a -> STATUS_COMPLETED.equalsIgnoreCase(a.getStatus())).count());
        stats.put("waiting", todayApps.stream().filter(a -> STATUS_CHECKED_IN.equalsIgnoreCase(a.getStatus())).count());
        stats.put("in_progress", todayApps.stream().filter(a -> STATUS_IN_PROGRESS.equalsIgnoreCase(a.getStatus())).count());

        return stats;
    }
    //Bắt đầu khám thì bác sĩ sẽ chạy hàm này
    @Transactional
    public void startDoctorExamination(Integer appointmentId, Integer doctorId) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn ID: " + appointmentId));
        // Check quyền sở hữu
        if (!app.getDoctor().getUser().getUserId().equals(doctorId)) {
            throw new AccessDeniedException("Đây không phải lịch hẹn của bạn.");
        }

        // Check trạng thái (Phải là đã Check-in thì mới Bắt đầu được)
        if (!STATUS_CHECKED_IN.equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException("Bệnh nhân chưa check-in hoặc trạng thái không hợp lệ.");
        }

        app.setStatus(STATUS_IN_PROGRESS);
        appointmentRepository.save(app);
    }
    //Lấy những bác sĩ đã từng khám cho bệnh nhân
    public List<DoctorProfileResponse> getPreviousDoctorsForPatient(UserDetailsImpl currentUser) {
        Integer patientId = currentUser.getId();

        // 1. Lấy danh sách các lịch hẹn đã Hoàn thành của bệnh nhân
        List<Appointment> completedAppointments = appointmentRepository.findByPatient_UserIdAndStatus(
                patientId, STATUS_COMPLETED);

        // 2. Trích xuất các đối tượng Doctor duy nhất từ danh sách này
        List<Doctor> distinctDoctors = completedAppointments.stream()
                .map(Appointment::getDoctor)
                .filter(java.util.Objects::nonNull) // Loại bỏ null nếu có
                .distinct() // Giữ lại các đối tượng Doctor duy nhất
                .collect(Collectors.toList());

        // 3. Map sang DTO để trả về (Bạn cần có DoctorMapper hoặc tự map thủ công)
        return distinctDoctors.stream()
                .map(this::convertToDoctorProfileResponse)
                .collect(Collectors.toList());
    }
    // Lấy toàn bộ lịch hẹn (Cho Admin)
    public List<AppointmentResponse> getAllAppointments() {
        List<Appointment> list = appointmentRepository.findAll();
        return list.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    private AppointmentResponse convertToResponse(Appointment app) {
        return appointmentMapper.toAppointmentResponse(app);
    }
    //Chuyển đối tượng Doctor Entity sang DoctorProfileResponse DTO an toàn
    private DoctorProfileResponse convertToDoctorProfileResponse(Doctor doctor) {
        DoctorProfileResponse response = new DoctorProfileResponse();

        // 1. Thông tin từ Doctor Entity
        response.setDoctorId(doctor.getDoctorId());
        response.setYearsOfExperience(doctor.getYearsOfExperience());
        // Giả sử Doctor có liên kết đến một bảng Specialty
        if (doctor.getSpecialty() != null) {
            response.setSpecialtyName(doctor.getSpecialty().getSpecialtyName());
        }

        // 2. Thông tin từ User Entity (liên kết qua Doctor.getUser())
        if (doctor.getUser() != null) {
            response.setFullName(doctor.getUser().getLastName() + " " + doctor.getUser().getFirstName());
            response.setPhoneNumber(doctor.getUser().getPhoneNumber());
            response.setEmail(doctor.getUser().getEmail());
        }

        return response;
    }
}