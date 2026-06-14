package com.example.medbook.controller;

import com.example.medbook.dto.request.SendNotificationRequest;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.dto.response.NotificationResponse;
import com.example.medbook.security.services.UserDetailsImpl;
import com.example.medbook.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // ==================== PATIENT APIs ====================

    @GetMapping("/my-notifications")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(notificationService.getMyNotifications(currentUser.getId()));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("Đã đánh dấu đã đọc"));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<MessageResponse> deleteMyNotification(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        notificationService.deleteMyNotification(id, currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("Xóa thông báo thành công"));
    }

    @DeleteMapping("/notifications/read")
    public ResponseEntity<MessageResponse> deleteAllReadNotifications(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        notificationService.deleteAllReadNotifications(currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("Đã xóa toàn bộ thông báo đã đọc"));
    }

    // ==================== ADMIN APIs ====================

    @GetMapping("/admin/notifications")
    public ResponseEntity<List<NotificationResponse>> getAllNotificationLogs() {
        return ResponseEntity.ok(notificationService.getAllNotificationLogs());
    }

    @PostMapping("/admin/notifications/send")
    public ResponseEntity<MessageResponse> sendManualNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        notificationService.sendManualNotification(request);
        return ResponseEntity.ok(new MessageResponse("Gửi thông báo thành công"));
    }

    @DeleteMapping("/admin/notifications/{id}")
    public ResponseEntity<MessageResponse> deleteNotificationLog(@PathVariable Integer id) {
        notificationService.deleteNotificationLog(id);
        return ResponseEntity.ok(new MessageResponse("Xóa log thông báo thành công"));
    }

    @DeleteMapping("/admin/notifications/delete-all")
    public ResponseEntity<MessageResponse> deleteAllNotificationLogs() {
        notificationService.deleteAllNotificationLogs();
        return ResponseEntity.ok(new MessageResponse("Xóa toàn bộ log thông báo thành công"));
    }

    @PostMapping("/admin/notifications/trigger-reminders")
    public ResponseEntity<MessageResponse> triggerReminders() {
        notificationService.triggerReminders();
        return ResponseEntity.ok(new MessageResponse("Đã kích hoạt nhắc nhở lịch khám thành công"));
    }
}
