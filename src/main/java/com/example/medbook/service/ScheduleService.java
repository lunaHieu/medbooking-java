package com.example.medbook.service;

import com.example.medbook.dto.request.ScheduleRequest;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.dto.response.ScheduleResponse;
import com.example.medbook.entity.Doctor;
import com.example.medbook.entity.DoctorAvailability;
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.mapper.ScheduleMapper;
import com.example.medbook.repository.DoctorAvailabilityRepository;
import com.example.medbook.repository.DoctorRepository;
import com.example.medbook.security.services.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private ScheduleMapper scheduleMapper;
    private static final String STATUS_BLOCKED = "Blocked";
    private static final int SLOT_DURATION_MINUTES = 30;
    @Transactional
    public ResponseEntity<MessageResponse> createDoctorSchedule(ScheduleRequest request, UserDetailsImpl currentUser) {
        Integer doctorId = currentUser.getId();
        Doctor doctor  = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ Bác sĩ"));

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        if(endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        //Băm thời gian ra làm 30p
        List<DoctorAvailability> slotsToSave = new ArrayList<>();
        LocalDateTime currentSlotStart = startTime;
        //Vòng lặp: chạy chừng nào slot tiếp theo (start + 30)
        while(!currentSlotStart.plusMinutes(SLOT_DURATION_MINUTES).isAfter(endTime)) {
            LocalDateTime currentSlotEnd = currentSlotStart.plusMinutes(SLOT_DURATION_MINUTES);
            DoctorAvailability slot = new DoctorAvailability();
            slot.setDoctor(doctor);
            slot.setStartTime(currentSlotStart);
            slot.setEndTime(currentSlotEnd);
            slot.setStatus("Available");

            slotsToSave.add(slot);

            currentSlotStart = currentSlotEnd;
        }

        if(!slotsToSave.isEmpty()) {
            availabilityRepository.saveAll(slotsToSave);
        }
        return ResponseEntity.ok(new MessageResponse("Đã tạo thành công " + slotsToSave.size() + " khung giờ rảnh."));
    }

    //Lấy lịch cho bác sĩ
    public List<ScheduleResponse> getMySchedules(UserDetailsImpl currentUser, LocalDate targetDate) {
        Integer doctorId = currentUser.getId();
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ Bác sĩ"));

        List<DoctorAvailability> availabilities;

        if (targetDate != null) {
            // Lọc theo ngày cụ thể (từ 00:00:00 đến 23:59:59)
            LocalDateTime start = targetDate.atStartOfDay();
            LocalDateTime end = targetDate.atTime(LocalTime.MAX);

            availabilities = availabilityRepository.findByDoctor_DoctorIdAndStartTimeBetween(
                    doctorId, start, end
            );
        } else {
            // Lấy toàn bộ lịch (logic cũ nếu không có ngày)
            availabilities = availabilityRepository.findByDoctor(doctor);
        }

        return scheduleMapper.toScheduleResponseList(availabilities);
    }
    public List<ScheduleResponse> getPublicAvailabilityByDate(Integer doctorId, LocalDate targetDate) {

        // 1. Kiểm tra DoctorID có tồn tại không (dùng lại logic cũ)
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bác sĩ với ID: " + doctorId));

        // 2. Định nghĩa khoảng thời gian (startOfDay -> endOfDay của targetDate)
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(java.time.LocalTime.MAX); // Cuối ngày

        // 3. Gọi Repository: Chỉ lấy các slot có Status = 'Available' trong khoảng 1 ngày
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctor_DoctorIdAndStatusAndStartTimeBetween(
                doctorId,
                "Available",
                start,
                end
        );

        // 4. Chuyển đổi sang DTO và trả về
        return scheduleMapper.toScheduleResponseList(availabilities);
    }
    //Xem lịch của bất kì ai
    public List<ScheduleResponse> getDoctorSchedulesByDate(Integer doctorId, LocalDate targetDate) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bác sĩ với ID: " + doctorId));

        if (targetDate == null) {
            // Admin phải chỉ định ngày để tránh tải toàn bộ dữ liệu lịch sử
            throw new IllegalArgumentException("Ngày xem lịch (targetDate) là bắt buộc.");
        }

        // Lọc theo ngày cụ thể (từ 00:00:00 đến 23:59:59)
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(LocalTime.MAX);

        return scheduleMapper.toScheduleResponseList(
                availabilityRepository.findByDoctor_DoctorIdAndStartTimeBetween(
                        doctorId, start, end
                )
        );
    }
    //Bác sĩ tự khóa lịch
    @Transactional
    public void cancelSlot(Integer slotId, Integer doctorId) {
        DoctorAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy slot ID: " + slotId));

        // Check quyền: Chỉ bác sĩ sở hữu slot mới được khóa
        if (!slot.getDoctor().getUser().getUserId().equals(doctorId)) {
            throw new AccessDeniedException("Bạn không có quyền khóa slot của bác sĩ khác.");
        }

        // Kiểm tra xem slot đã có lịch hẹn chưa
        if ("Booked".equalsIgnoreCase(slot.getStatus())) {
            // Tùy theo logic nghiệp vụ: có thể cho khóa và tự động hủy lịch, hoặc bắt hủy tay
            throw new IllegalStateException("Slot này đã có lịch hẹn, không thể khóa. Cần hủy lịch hẹn đó trước.");
        }

        // Đặt trạng thái khóa
        slot.setStatus(STATUS_BLOCKED);
        availabilityRepository.save(slot);
    }
    //LẤY LỊCH RẢNH THEO KHOẢNG NGÀY (Mặc định là Hôm nay)
    public List<ScheduleResponse> getPublicAvailableSlots(Integer doctorId, Integer days) {

        //Kiểm tra DoctorID có tồn tại không
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bác sĩ với ID: " + doctorId));

        //Tính toán khoảng thời gian
        LocalDate today = LocalDate.now();

        // Nếu 'days' là null -> Lấy 1 ngày (Hôm nay)
        // Nếu 'days' có giá trị -> Lấy n ngày tới
        LocalDate futureDate = today.plusDays(days != null ? days : 1);

        // Từ đầu ngày hôm nay (00:00:00)
        LocalDateTime start = today.atStartOfDay();
        // Đến cuối ngày tương lai (23:59:59)
        LocalDateTime end = futureDate.atTime(LocalTime.MAX);

        //Gọi Repository: Chỉ lấy các slot có Status = 'Available' trong khoảng thời gian này
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctor_DoctorIdAndStatusAndStartTimeBetween(
                doctorId,
                "Available",
                start,
                end
        );

        //Chuyển đổi sang DTO và trả về
        return scheduleMapper.toScheduleResponseList(availabilities);
    }
    // Hàm cho Staff khóa slot (Không check quyền sở hữu slot)
    @Transactional
    public void blockSlotByStaff(Integer slotId) {
        DoctorAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy slot ID: " + slotId));

        if ("Booked".equalsIgnoreCase(slot.getStatus())) {
            throw new IllegalStateException("Slot này đã có khách đặt.");
        }
        slot.setStatus("Blocked");
        availabilityRepository.save(slot);
    }
    //TÌM SLOT RẢNH THEO CHUYÊN KHOA
    public List<ScheduleResponse> getAvailableSlotsBySpecialty(Integer specialtyId, LocalDate targetDate) {

        //Xác định thời gian (Mặc định là hôm nay nếu không gửi ngày)
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(java.time.LocalTime.MAX);

        //Gọi Repository: Tìm tất cả slot Available thuộc Chuyên khoa này trong ngày đó
        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctor_Specialty_SpecialtyIdAndStatusAndStartTimeBetween(
                        specialtyId,
                        "Available",
                        start,
                        end
                );

        //Map sang DTO
        // (Lưu ý: Kết quả có thể chứa nhiều slot cùng giờ của các bác sĩ khác nhau.
        // Ví dụ: 8:00 (Dr A), 8:00 (Dr B). Frontend sẽ lo việc hiển thị).
        return scheduleMapper.toScheduleResponseList(availabilities);
    }
    //ADMIN TẠO LỊCH CHO BÁC SĨ
    @Transactional
    public ResponseEntity<MessageResponse> createScheduleForDoctor(Integer doctorId, ScheduleRequest request) {
        // 1. Admin chọn bác sĩ cụ thể
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bác sĩ ID: " + doctorId));

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();

        // (Logic kiểm tra thời gian giống hệt hàm cũ)
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        // (Logic băm slot 30 phút giống hệt hàm cũ)
        List<DoctorAvailability> slotsToSave = new ArrayList<>();
        LocalDateTime currentSlotStart = startTime;

        while (!currentSlotStart.plusMinutes(30).isAfter(endTime)) { // 30 là SLOT_DURATION_MINUTES
            LocalDateTime currentSlotEnd = currentSlotStart.plusMinutes(30);

            DoctorAvailability slot = new DoctorAvailability();
            slot.setDoctor(doctor); // Gán cho Bác sĩ đã chọn
            slot.setStartTime(currentSlotStart);
            slot.setEndTime(currentSlotEnd);
            slot.setStatus("Available");

            slotsToSave.add(slot);
            currentSlotStart = currentSlotEnd;
        }

        if (!slotsToSave.isEmpty()) {
            availabilityRepository.saveAll(slotsToSave);
        }

        return ResponseEntity.ok(new MessageResponse("Admin đã tạo thành công " + slotsToSave.size() + " slot cho Bác sĩ " + doctor.getUser().getLastName()));
    }
    //ADMIN HỦY SLOT BẤT KỲ
    public void cancelSlotByAdmin(Integer slotId) {
        DoctorAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy slot ID: " + slotId));

        // Admin không cần kiểm tra slot.getDoctor().equals(currentUser)

        // Nếu slot đã có người đặt, Admin vẫn có thể hủy
        //cho phép hủy và đổi thành Blocked
        slot.setStatus("Blocked");
        availabilityRepository.save(slot);
    }
}
