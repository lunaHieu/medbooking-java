package com.example.medbook.scheduler;

import com.example.medbook.entity.Appointment;
import com.example.medbook.repository.AppointmentRepository;
import com.example.medbook.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class AppointmentReminderScheduler {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationService notificationService;

    // Chạy mỗi ngày vào lúc 8:00 Sáng
    // Cron expression: "Giây Phút Giờ Ngày Tháng Thứ"
    @Scheduled(cron = "0 0 8 * * ?")
    // @Scheduled(fixedRate = 60000)
    public void sendDailyReminders() {

        // 1. Xác định thời gian "Ngày mai" (Từ 00:00 đến 23:59)
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime startOfTomorrow = tomorrow.atStartOfDay();
        LocalDateTime endOfTomorrow = tomorrow.atTime(LocalTime.MAX);

        // 2. Tìm các lịch hẹn "Confirmed" vào ngày mai
        List<Appointment> appointments = appointmentRepository.findByStatusAndStartTimeBetween(
                "Confirmed", // Chỉ nhắc lịch đã xác nhận
                startOfTomorrow,
                endOfTomorrow
        );

        // 3. Gửi thông báo cho từng người
        for (Appointment app : appointments) {
            try {
                notificationService.sendReminder(app);
            } catch (Exception e) {
                // ignore or handle silently
            }
        }
    }
}