package com.example.medbook.service;

import com.example.medbook.dto.request.FeedbackRequest;
import com.example.medbook.dto.response.RatingSummaryResponse;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.dto.response.FeedbackResponse; // <-- THÊM IMPORT
import com.example.medbook.entity.Appointment;
import com.example.medbook.entity.Feedbacks;
import com.example.medbook.entity.User; // <-- THÊM IMPORT
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.mapper.FeedbackMapper; // <-- THÊM IMPORT
import com.example.medbook.repository.AppointmentRepository;
import com.example.medbook.repository.FeedbackRepository;
import com.example.medbook.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors; // <-- THÊM IMPORT

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Transactional
    public ResponseEntity<MessageResponse> submitFeedback(FeedbackRequest request, UserDetailsImpl currentUser) {

        //Tìm và Kiểm tra Lịch hẹn (Giữ nguyên)
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Lịch hẹn ID: " + request.getAppointmentId()));

        if (!appointment.getPatient().getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không phải Bệnh nhân của lịch hẹn này.");
        }

        if (!"Completed".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Chỉ có thể đánh giá lịch hẹn đã hoàn tất (Completed).");
        }

        if (feedbackRepository.findByAppointment_AppointmentId(appointment.getAppointmentId()).isPresent()) {
            throw new IllegalStateException("Lịch hẹn này đã được đánh giá rồi.");
        }


        // Lấy loại đánh giá từ Request. Nếu null hoặc rỗng thì mặc định là "DOCTOR"
        String type = (request.getTargetType() == null || request.getTargetType().isEmpty())
                ? "DOCTOR"
                : request.getTargetType().toUpperCase();

        Integer targetId = 0; // Mặc định ID = 0

        if ("DOCTOR".equals(type)) {
            // Nếu là đánh giá Bác sĩ, lấy ID của Bác sĩ từ lịch hẹn
            targetId = appointment.getDoctor().getDoctorId();
        } else if ("SYSTEM".equals(type)) {
            // Nếu là đánh giá Hệ thống, targetId có thể là 0 (hoặc 1 ID quy ước)
            targetId = 0;
        } else {
            throw new IllegalArgumentException("Loại đánh giá không hợp lệ. Chỉ chấp nhận 'DOCTOR' hoặc 'SYSTEM'.");
        }

        // 5. Tạo Feedback Entity
        Feedbacks feedback = new Feedbacks();
        feedback.setAppointment(appointment);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());

        // Gán thông tin đã xử lý
        feedback.setTargetType(type);
        feedback.setTargetId(targetId);

        feedbackRepository.save(feedback);

        return ResponseEntity.ok(new MessageResponse("Cảm ơn bạn đã gửi đánh giá (" + type + ")!"));
    }
    //ADMIN: LỌC FEEDBACK
    public List<FeedbackResponse> getFeedbacksForAdmin(String type) {
        List<Feedbacks> list;

        if (type != null && !type.isEmpty()) {
            // Lọc theo loại (ví dụ: chỉ xem SYSTEM)
            list = feedbackRepository.findByTargetTypeOrderByCreatedAtDesc(type);
        } else {
            // Xem tất cả
            list = feedbackRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        }

        return list.stream().map(this::mapFeedbackToDetailedResponse).collect(Collectors.toList());
    }
    //LẤY CHI TIẾT TẤT CẢ FEEDBACK CHO MỘT BÁC SĨ
    public List<FeedbackResponse> getDetailedDoctorFeedback(Integer doctorId) {

        // 1. Lấy tất cả feedback cho doctor đó từ Repository
        List<Feedbacks> feedbackList = feedbackRepository.findByDoctorId(doctorId);

        // 2. Chuyển đổi và gán tên thủ công
        return feedbackList.stream()
                .map(this::mapFeedbackToDetailedResponse)
                .collect(Collectors.toList());
    }

    //LẤY TỔNG HỢP RATING CHO PUBLIC
    public RatingSummaryResponse getDoctorRatingSummary(Integer doctorId) {
        Double avgRating = feedbackRepository.findAverageRatingByDoctorId(doctorId);
        List<Feedbacks> reviews = feedbackRepository.findByDoctorId(doctorId);
        long totalReviews = reviews.size();
        RatingSummaryResponse response = new RatingSummaryResponse();
        response.setTargetId(doctorId);
        response.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        response.setTotalReviews(totalReviews);
        return response;
    }
    //ADMIN XEM TẤT CẢ ĐÁNH GIÁ
    public List<FeedbackResponse> getAllFeedbacksForAdmin() {
        List<Feedbacks> allFeedbacks = feedbackRepository.findAllByOrderByCreatedAtDesc();

        // Tái sử dụng hàm helper mapFeedbackToDetailedResponse để có đủ tên Bác sĩ/Bệnh nhân
        return allFeedbacks.stream()
                .map(this::mapFeedbackToDetailedResponse)
                .collect(Collectors.toList());
    }
    //LẤY FEEDBACK CHO TRANG CHỦ
    public List<FeedbackResponse> getFeaturedFeedbacks() {
        // Lấy các đánh giá từ 4 sao trở lên
        List<Feedbacks> topFeedbacks = feedbackRepository.findTop10ByRatingGreaterThanEqualOrderByCreatedAtDesc(4);

        // Chuyển đổi sang DTO (có tên Bệnh nhân để hiển thị)
        return topFeedbacks.stream()
                .map(this::mapFeedbackToDetailedResponse)
                .collect(Collectors.toList());
    }
    // --- HÀM PHỤ TRỢ (Helper) ---
    private FeedbackResponse mapFeedbackToDetailedResponse(Feedbacks feedback) {

        // 1. Dùng MapStruct để map các trường cơ bản
        FeedbackResponse response = feedbackMapper.toFeedbackResponse(feedback);

        // 2. Gán tên Bệnh nhân (từ Appointment -> Patient -> User)
        if (feedback.getAppointment() != null && feedback.getAppointment().getPatient() != null) {
            User patient = feedback.getAppointment().getPatient();
            response.setPatientName(patient.getLastName() + " " + patient.getFirstName());
        }

        // 3. Gán tên Bác sĩ
        if (feedback.getAppointment() != null && feedback.getAppointment().getDoctor() != null) {
            User doctorUser = feedback.getAppointment().getDoctor().getUser();
            response.setDoctorName(doctorUser.getLastName() + " " + doctorUser.getFirstName());
        }

        return response;
    }

}