-- V1__init_schema.sql

-- TABLE: users
CREATE TABLE IF NOT EXISTS `users` (
  `UserID` int NOT NULL AUTO_INCREMENT,
  `Address` varchar(255) DEFAULT NULL,
  `AvatarURL` varchar(255) DEFAULT NULL,
  `DateOfBirth` date DEFAULT NULL,
  `Email` varchar(255) DEFAULT NULL,
  `FirstName` varchar(255) NOT NULL,
  `Gender` varchar(255) DEFAULT NULL,
  `LastName` varchar(255) NOT NULL,
  `PasswordHash` varchar(255) NOT NULL,
  `PhoneNumber` varchar(255) NOT NULL,
  `Role` varchar(255) NOT NULL,
  `Status` varchar(255) NOT NULL,
  `Username` varchar(255) NOT NULL,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `UKaoqn387iw3a0y4ewpxvhqpr3a` (`PhoneNumber`),
  UNIQUE KEY `UKtbpk8dt8cn41jgc3c5cjmulf1` (`Username`),
  UNIQUE KEY `UKgnfv1k6flrriv6a9jh5cja03x` (`Email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: specialties
CREATE TABLE IF NOT EXISTS `specialties` (
  `SpecialtyID` int NOT NULL AUTO_INCREMENT,
  `Description` text,
  `ImageURL` varchar(255) DEFAULT NULL,
  `SpecialtyName` varchar(255) NOT NULL,
  PRIMARY KEY (`SpecialtyID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: doctors
CREATE TABLE IF NOT EXISTS `doctors` (
  `DoctorID` int NOT NULL,
  `Degree` varchar(255) DEFAULT NULL,
  `ImageURL` varchar(255) DEFAULT NULL,
  `ProfileDescription` varchar(255) DEFAULT NULL,
  `YearsOfExperience` int DEFAULT NULL,
  `SpecialtyID` int NOT NULL,
  PRIMARY KEY (`DoctorID`),
  KEY `FKc595xa8fa40er7a9x1wd7c95i` (`SpecialtyID`),
  CONSTRAINT `FKc595xa8fa40er7a9x1wd7c95i` FOREIGN KEY (`SpecialtyID`) REFERENCES `specialties` (`SpecialtyID`),
  CONSTRAINT `FKc9b92q9uc412ul0jsax3cnvk3` FOREIGN KEY (`DoctorID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: services
CREATE TABLE IF NOT EXISTS `services` (
  `ServiceID` int NOT NULL AUTO_INCREMENT,
  `Description` text,
  `EstimatedDuration` int DEFAULT NULL,
  `ImageURL` varchar(255) DEFAULT NULL,
  `Price` decimal(18,2) DEFAULT NULL,
  `ServiceName` varchar(255) NOT NULL,
  `SpecialtyID` int DEFAULT NULL,
  PRIMARY KEY (`ServiceID`),
  KEY `FKcce7clfs1fi9rek2j9xhtw1hq` (`SpecialtyID`),
  CONSTRAINT `FKcce7clfs1fi9rek2j9xhtw1hq` FOREIGN KEY (`SpecialtyID`) REFERENCES `specialties` (`SpecialtyID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: doctoravailability
CREATE TABLE IF NOT EXISTS `doctoravailability` (
  `SlotID` int NOT NULL AUTO_INCREMENT,
  `EndTime` datetime(6) DEFAULT NULL,
  `StartTime` datetime(6) DEFAULT NULL,
  `Status` varchar(255) DEFAULT NULL,
  `DoctorID` int NOT NULL,
  PRIMARY KEY (`SlotID`),
  KEY `FK9m49mt7dxhwysx1id2v55crqs` (`DoctorID`),
  CONSTRAINT `FK9m49mt7dxhwysx1id2v55crqs` FOREIGN KEY (`DoctorID`) REFERENCES `doctors` (`DoctorID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: appointments
CREATE TABLE IF NOT EXISTS `appointments` (
  `AppointmentID` int NOT NULL AUTO_INCREMENT,
  `CancellationReason` varchar(255) DEFAULT NULL,
  `CreatedAt` datetime(6) DEFAULT NULL,
  `EstimatedDuration` int DEFAULT NULL,
  `InitialSymptoms` varchar(255) DEFAULT NULL,
  `StartTime` datetime(6) DEFAULT NULL,
  `Status` varchar(255) DEFAULT NULL,
  `DoctorID` int NOT NULL,
  `PatientID` int NOT NULL,
  `ServiceID` int DEFAULT NULL,
  `SlotID` int DEFAULT NULL,
  PRIMARY KEY (`AppointmentID`),
  UNIQUE KEY `UK3ywkheu5vukk5jl6oan2awnog` (`SlotID`),
  KEY `FKkd7v5cuabio9vemq19bqhwxod` (`DoctorID`),
  KEY `FKdurqcyi3wvds43p3856mqeu9a` (`PatientID`),
  KEY `FKjqma2tq04wr8rf8bghd7qkt5q` (`ServiceID`),
  CONSTRAINT `FKb82cmwxaynr1fccoqyf6m9jj1` FOREIGN KEY (`SlotID`) REFERENCES `doctoravailability` (`SlotID`),
  CONSTRAINT `FKdurqcyi3wvds43p3856mqeu9a` FOREIGN KEY (`PatientID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `FKjqma2tq04wr8rf8bghd7qkt5q` FOREIGN KEY (`ServiceID`) REFERENCES `services` (`ServiceID`),
  CONSTRAINT `FKkd7v5cuabio9vemq19bqhwxod` FOREIGN KEY (`DoctorID`) REFERENCES `doctors` (`DoctorID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: feedbacks
CREATE TABLE IF NOT EXISTS `feedbacks` (
  `FeedbackID` int NOT NULL AUTO_INCREMENT,
  `Comment` text,
  `CreatedAt` datetime(6) DEFAULT NULL,
  `Rating` int NOT NULL,
  `TargetID` int DEFAULT NULL,
  `TargetType` varchar(255) DEFAULT NULL,
  `AppointmentID` int NOT NULL,
  PRIMARY KEY (`FeedbackID`),
  UNIQUE KEY `UKhfi34lx0ylc90vsj2rrsjjg6f` (`AppointmentID`),
  CONSTRAINT `FK4h0qoyo1hidyyw9g8gr87i6at` FOREIGN KEY (`AppointmentID`) REFERENCES `appointments` (`AppointmentID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: medicalrecords
CREATE TABLE IF NOT EXISTS `medicalrecords` (
  `RecordID` int NOT NULL AUTO_INCREMENT,
  `CreatedAt` datetime(6) DEFAULT NULL,
  `Diagnosis` text,
  `Notes` text,
  `AppointmentID` int NOT NULL,
  `DoctorID` int NOT NULL,
  `PatientID` int NOT NULL,
  PRIMARY KEY (`RecordID`),
  UNIQUE KEY `UKlw85vu5r94rf54hno41act0bf` (`AppointmentID`),
  KEY `FK7j6dkv3l5b8tldiinb8n4fcw6` (`DoctorID`),
  KEY `FKi1t6fm3e8ksy6fg449jnibgiq` (`PatientID`),
  CONSTRAINT `FK7j6dkv3l5b8tldiinb8n4fcw6` FOREIGN KEY (`DoctorID`) REFERENCES `doctors` (`DoctorID`),
  CONSTRAINT `FKi1t6fm3e8ksy6fg449jnibgiq` FOREIGN KEY (`PatientID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `FKp6su3tlwdj1ri6u3higmkmhcj` FOREIGN KEY (`AppointmentID`) REFERENCES `appointments` (`AppointmentID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: examresults
CREATE TABLE IF NOT EXISTS `examresults` (
  `ResultID` int NOT NULL AUTO_INCREMENT,
  `FileDescription` varchar(255) DEFAULT NULL,
  `FilePath` varchar(255) NOT NULL,
  `FileType` varchar(255) DEFAULT NULL,
  `UploadedAt` datetime(6) DEFAULT NULL,
  `RecordID` int NOT NULL,
  PRIMARY KEY (`ResultID`),
  KEY `FKnsgmw7rsl4581fvomurgnm51b` (`RecordID`),
  CONSTRAINT `FKnsgmw7rsl4581fvomurgnm51b` FOREIGN KEY (`RecordID`) REFERENCES `medicalrecords` (`RecordID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: notifications
CREATE TABLE IF NOT EXISTS `notifications` (
  `notificationId` int NOT NULL AUTO_INCREMENT,
  `channel` varchar(255) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `notificationType` varchar(255) DEFAULT NULL,
  `sentAt` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `AppointmentID` int DEFAULT NULL,
  `UserID` int DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`notificationId`),
  KEY `FK8ttev7wqt26x75pb7020ikb0p` (`AppointmentID`),
  KEY `FK228hvlwq2m7ter6n9nnsrwr73` (`UserID`),
  CONSTRAINT `FK228hvlwq2m7ter6n9nnsrwr73` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `FK8ttev7wqt26x75pb7020ikb0p` FOREIGN KEY (`AppointmentID`) REFERENCES `appointments` (`AppointmentID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: otps
CREATE TABLE IF NOT EXISTS `otps` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `otp` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TABLE: user_relations
CREATE TABLE IF NOT EXISTS `user_relations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `RelationType` varchar(255) DEFAULT NULL,
  `RelativeUserID` int NOT NULL,
  `UserID` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9v36mlrbyit6cxnq77964e6y0` (`UserID`,`RelativeUserID`),
  KEY `FKsw4x637fo2e8nw8xf1k6w2n9e` (`RelativeUserID`),
  CONSTRAINT `FKgujftenuimuny8mcjmxdmqj5y` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `FKsw4x637fo2e8nw8xf1k6w2n9e` FOREIGN KEY (`RelativeUserID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
