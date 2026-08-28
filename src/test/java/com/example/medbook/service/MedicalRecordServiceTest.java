package com.example.medbook.service;

import com.example.medbook.dto.request.MedicalRecordRequest;
import com.example.medbook.dto.response.MedicalRecordResponse;
import com.example.medbook.entity.Appointment;
import com.example.medbook.entity.Doctor;
import com.example.medbook.entity.MedicalRecord;
import com.example.medbook.entity.User;
import com.example.medbook.mapper.MedicalRecordMapper;
import com.example.medbook.repository.AppointmentRepository;
import com.example.medbook.repository.ExamResultsRepository;
import com.example.medbook.repository.MedicalRecordRepository;
import com.example.medbook.repository.UserRepository;
import com.example.medbook.security.services.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private MedicalRecordMapper medicalRecordMapper;
    @Mock private ExamResultsRepository examResultsRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks private MedicalRecordService medicalRecordService;

    @Test
    void createsRecordForItsDoctorWhileAppointmentIsInProgress() {
        Appointment appointment = appointmentOwnedBy(1000, "InProgress");
        MedicalRecordResponse response = new MedicalRecordResponse();
        when(appointmentRepository.findById(42)).thenReturn(Optional.of(appointment));
        when(medicalRecordRepository.findByAppointment_AppointmentId(42)).thenReturn(Optional.empty());
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(medicalRecordMapper.toMedicalRecordResponse(any(MedicalRecord.class))).thenReturn(response);

        MedicalRecordResponse actual = medicalRecordService.createMedicalRecord(
                requestFor(42),
                new UserDetailsImpl(Integer.valueOf(1000), "doctor", "", Collections.emptyList())
        );

        ArgumentCaptor<MedicalRecord> saved = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(medicalRecordRepository).save(saved.capture());
        assertEquals("Viêm họng", saved.getValue().getDiagnosis());
        assertEquals("Nghỉ ngơi", saved.getValue().getNotes());
        assertEquals(appointment, saved.getValue().getAppointment());
        assertEquals(response, actual);
    }

    @Test
    void rejectsRecordBeforeExaminationStarts() {
        when(appointmentRepository.findById(42)).thenReturn(Optional.of(appointmentOwnedBy(1000, "Confirmed")));

        assertThrows(IllegalStateException.class, () -> medicalRecordService.createMedicalRecord(
                requestFor(42),
                new UserDetailsImpl(1000, "doctor", "", Collections.emptyList())
        ));

        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void rejectsRecordCreatedByAnotherDoctor() {
        when(appointmentRepository.findById(42)).thenReturn(Optional.of(appointmentOwnedBy(1000, "InProgress")));

        assertThrows(AccessDeniedException.class, () -> medicalRecordService.createMedicalRecord(
                requestFor(42),
                new UserDetailsImpl(1001, "other-doctor", "", Collections.emptyList())
        ));

        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    private Appointment appointmentOwnedBy(int doctorId, String status) {
        User doctorUser = new User();
        doctorUser.setUserId(doctorId);
        doctorUser.setFirstName("Bác sĩ");
        doctorUser.setLastName("Nguyễn");

        Doctor doctor = new Doctor();
        doctor.setDoctorId(doctorId);
        doctor.setUser(doctorUser);

        User patient = new User();
        patient.setUserId(2000);
        patient.setFirstName("Bệnh nhân");
        patient.setLastName("Trần");

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(42);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStatus(status);
        return appointment;
    }

    private MedicalRecordRequest requestFor(int appointmentId) {
        MedicalRecordRequest request = new MedicalRecordRequest();
        request.setAppointmentId(appointmentId);
        request.setDiagnosis("Viêm họng");
        request.setNotes("Nghỉ ngơi");
        return request;
    }
}
