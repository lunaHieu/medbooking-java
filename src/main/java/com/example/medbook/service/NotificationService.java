package com.example.medbook.service;

import com.example.medbook.entity.Appointment;
import com.example.medbook.entity.Notification;
import com.example.medbook.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Hàm gửi thông báo nhắc nhở
    public void sendReminder(Appointment appointment) {
        // 1. Tạo nội dung thông báo
        String message = "Xin chào " + appointment.getPatient().getFirstName() +
                ", bạn có lịch khám vào ngày mai lúc " + appointment.getStartTime() +
                " với Bác sĩ " + appointment.getDoctor().getUser().getLastName();

        // 2. Lưu vào CSDL (Để hiện lên chuông thông báo trên Web)
        Notification notif = new Notification();
        notif.setUser(appointment.getPatient()); // Người nhận là Bệnh nhân
        notif.setAppointment(appointment);
        notif.setNotificationType("Reminder");
        notif.setContent(message);
        notif.setChannel("System");
        notif.setSentAt(LocalDateTime.now());
        notif.setStatus("Sent");

        notificationRepository.save(notif);

        // 3. Giả lập gửi Email (In ra console)
//        System.out.println("[EMAIL SENT] To: " + appointment.getPatient().getEmail());
//        System.out.println("   Subject: Nhắc nhở lịch khám");
//        System.out.println("   Body: " + message);
//        System.out.println("--------------------------------------------------");
    }
}