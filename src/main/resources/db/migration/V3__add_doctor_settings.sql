CREATE TABLE IF NOT EXISTS doctor_settings (
  DoctorID int NOT NULL,
  NotificationSettings text NOT NULL,
  Preferences text NOT NULL,
  PRIMARY KEY (DoctorID),
  CONSTRAINT FK_doctor_settings_doctor FOREIGN KEY (DoctorID) REFERENCES doctors (DoctorID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
