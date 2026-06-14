package com.example.medbook.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Data
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @JsonIgnore
    @ManyToOne @JoinColumn(name = "UserID")
    private User user;

    @JsonIgnore
    @ManyToOne @JoinColumn(name = "AppointmentID")
    private Appointment appointment;

    private String title;
    private String notificationType;
    private String content;
    private String channel;
    private LocalDateTime sentAt;
    private String status;
}