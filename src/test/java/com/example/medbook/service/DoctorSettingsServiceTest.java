package com.example.medbook.service;

import com.example.medbook.dto.request.DoctorSettingsRequest;
import com.example.medbook.dto.response.DoctorSettingsResponse;
import com.example.medbook.entity.DoctorSettings;
import com.example.medbook.mapper.DoctorMapper;
import com.example.medbook.repository.DoctorRepository;
import com.example.medbook.repository.DoctorSettingsRepository;
import com.example.medbook.repository.ServiceRepository;
import com.example.medbook.repository.SpecialtyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorSettingsServiceTest {

    @Mock private DoctorRepository doctorRepository;
    @Mock private DoctorSettingsRepository doctorSettingsRepository;
    @Mock private DoctorMapper doctorMapper;
    @Mock private ServiceRepository serviceRepository;
    @Mock private SpecialtyRepository specialtyRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private DoctorService doctorService;

    @Test
    void storesAndReadsSettingsForTheAuthenticatedDoctor() {
        Map<String, Object> notifications = Map.of("quietHours", Map.of("enabled", true));
        Map<String, Object> preferences = Map.of("theme", "dark");
        when(doctorRepository.existsById(10)).thenReturn(true);
        when(doctorSettingsRepository.findById(10)).thenReturn(Optional.empty());
        when(doctorSettingsRepository.save(any(DoctorSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorSettingsResponse saved = doctorService.updateMySettings(
                10,
                new DoctorSettingsRequest(notifications, preferences)
        );

        ArgumentCaptor<DoctorSettings> captor = ArgumentCaptor.forClass(DoctorSettings.class);
        verify(doctorSettingsRepository).save(captor.capture());
        assertEquals(10, captor.getValue().getDoctorId());
        assertEquals(notifications, saved.notificationSettings());
        assertEquals(preferences, saved.preferences());

        when(doctorSettingsRepository.findById(10)).thenReturn(Optional.of(captor.getValue()));
        DoctorSettingsResponse loaded = doctorService.getMySettings(10);
        assertEquals(notifications, loaded.notificationSettings());
        assertEquals(preferences, loaded.preferences());
    }
}
