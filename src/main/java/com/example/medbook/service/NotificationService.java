package com.example.medbook.service;

import com.example.medbook.dto.request.SendNotificationRequest;
import com.example.medbook.dto.response.NotificationResponse;
import com.example.medbook.entity.Appointment;
import com.example.medbook.entity.Notification;
import com.example.medbook.entity.User;
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.repository.AppointmentRepository;
import com.example.medbook.repository.NotificationRepository;
import com.example.medbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private NotificationResponse convertToResponse(Notification notif) {
        NotificationResponse res = new NotificationResponse();
        res.setNotificationId(notif.getNotificationId());
        res.setUserId(notif.getUser() != null ? notif.getUser().getUserId() : null);
        res.setTitle(notif.getTitle());
        res.setContent(notif.getContent());
        res.setNotificationType(notif.getNotificationType());
        res.setChannel(notif.getChannel());
        res.setStatus(notif.getStatus());
        res.setCreatedAt(notif.getSentAt());
        return res;
    }

    private List<NotificationResponse> convertToResponseList(List<Notification> list) {
        return list.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<NotificationResponse> getMyNotifications(Integer userId) {
        return convertToResponseList(notificationRepository.findByUser_UserIdOrderBySentAtDesc(userId));
    }

    @Transactional
    public void markAsRead(Integer id, Integer userId) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + id));
        if (notif.getUser() == null || !notif.getUser().getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền cập nhật thông báo này.");
        }
        notif.setStatus("Read");
        notificationRepository.save(notif);
    }

    @Transactional
    public void deleteMyNotification(Integer id, Integer userId) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + id));
        if (notif.getUser() == null || !notif.getUser().getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền xóa thông báo này.");
        }
        notificationRepository.delete(notif);
    }

    @Transactional
    public void deleteAllReadNotifications(Integer userId) {
        notificationRepository.deleteReadByUserId(userId);
    }

    public List<NotificationResponse> getAllNotificationLogs() {
        return convertToResponseList(notificationRepository.findAllByOrderBySentAtDesc());
    }

    @Transactional
    public void sendManualNotification(SendNotificationRequest request) {
        String targetGroup = request.getTargetGroup().toLowerCase();
        List<User> targetUsers;

        if ("patients".equals(targetGroup)) {
            targetUsers = userRepository.findAll().stream()
                    .filter(u -> "PATIENT".equalsIgnoreCase(u.getRole()))
                    .collect(Collectors.toList());
        } else if ("doctors".equals(targetGroup)) {
            targetUsers = userRepository.findAll().stream()
                    .filter(u -> "DOCTOR".equalsIgnoreCase(u.getRole()))
                    .collect(Collectors.toList());
        } else if ("staff".equals(targetGroup)) {
            targetUsers = userRepository.findAll().stream()
                    .filter(u -> "MEDICAL_STAFF".equalsIgnoreCase(u.getRole()))
                    .collect(Collectors.toList());
        } else { // "all"
            targetUsers = userRepository.findAll();
        }

        String channel = "Email".equalsIgnoreCase(request.getChannel()) ? "Email" : "System";

        for (User user : targetUsers) {
            Notification notif = new Notification();
            notif.setUser(user);
            notif.setTitle(request.getTitle());
            notif.setContent(request.getContent());
            notif.setNotificationType("System");
            notif.setChannel(channel);
            notif.setSentAt(LocalDateTime.now());
            notif.setStatus("Unread");
            notificationRepository.save(notif);

            if ("Email".equalsIgnoreCase(channel) && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                sendEmailQuietly(user.getEmail(), request.getTitle(), request.getContent());
            }
        }
    }

    @Transactional
    public void deleteNotificationLog(Integer id) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo log với ID: " + id));
        notificationRepository.delete(notif);
    }

    @Transactional
    public void deleteAllNotificationLogs() {
        notificationRepository.deleteAll();
    }

    // Hàm gửi thông báo nhắc nhở
    @Transactional
    public void sendReminder(Appointment appointment) {
        String message = "Xin chào " + appointment.getPatient().getFirstName() +
                ", bạn có lịch khám vào ngày mai lúc " + appointment.getStartTime() +
                " với Bác sĩ " + appointment.getDoctor().getUser().getLastName();

        Notification notif = new Notification();
        notif.setUser(appointment.getPatient());
        notif.setAppointment(appointment);
        notif.setTitle("Nhắc nhở lịch khám");
        notif.setNotificationType("Reminder");
        notif.setContent(message);
        notif.setChannel("System");
        notif.setSentAt(LocalDateTime.now());
        notif.setStatus("Unread");

        notificationRepository.save(notif);

        // also send email if patient has email
        if (appointment.getPatient().getEmail() != null && !appointment.getPatient().getEmail().trim().isEmpty()) {
            sendEmailQuietly(appointment.getPatient().getEmail(), "Nhắc nhở lịch khám", message);
        }
    }

    @Transactional
    public void triggerReminders() {
        LocalDateTime startOfTomorrow = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfTomorrow = startOfTomorrow.plusDays(1).minusNanos(1);

        List<Appointment> tomorrowAppointments = appointmentRepository.findByStatusAndStartTimeBetween(
                "Confirmed", startOfTomorrow, endOfTomorrow);

        for (Appointment app : tomorrowAppointments) {
            sendReminder(app);
        }
    }

    private void sendEmailQuietly(String to, String subject, String body) {
        if (mailSender == null) {
            System.out.println("[SMTP MOCK] to=" + to + ", subject=" + subject + ", body=" + body);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Gửi mail thất bại cho " + to + ": " + e.getMessage());
        }
    }
}