package com.example.medbook.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Data
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @ManyToOne @JoinColumn(name = "UserID")
    private User user;

    @ManyToOne @JoinColumn(name = "AppointmentID")
    private Appointment appointment;

    private String notificationType;
    private String content;
    private String channel;
    private LocalDateTime sentAt;
    private String status;
}