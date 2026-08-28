package com.example.medbook.service;

import com.example.medbook.dto.request.DoctorProfileUpdateRequest;
import com.example.medbook.dto.request.DoctorSettingsRequest;
import com.example.medbook.dto.response.DoctorProfileResponse;
import com.example.medbook.dto.response.DoctorSettingsResponse;
import com.example.medbook.entity.Doctor;
import com.example.medbook.entity.DoctorSettings;
import com.example.medbook.entity.Service;
import com.example.medbook.entity.Specialty;
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.mapper.DoctorMapper;
import com.example.medbook.repository.DoctorRepository;
import com.example.medbook.repository.DoctorSettingsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.medbook.repository.ServiceRepository;
import com.example.medbook.repository.SpecialtyRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@org.springframework.stereotype.Service
public class DoctorService {
    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private SpecialtyRepository specialtyRepository;
    @Autowired
    private DoctorSettingsRepository doctorSettingsRepository;
    @Autowired
    private ObjectMapper objectMapper;
    public List<DoctorProfileResponse> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        return doctorMapper.toDoctorProfileResponseList(doctors);
    }

    public DoctorProfileResponse getDoctorById(Integer id){
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Bác sĩ với ID: " + id));
        return doctorMapper.toDoctorProfileResponse(doctor);
    }
    public List<DoctorProfileResponse> searchDoctor(String query, Integer specialtyId){
        List<Doctor> doctors;
        if (specialtyId != null) {
            // Lọc theo Chuyên khoa ID (Sẽ cần hàm findBySpecialty_SpecialtyId trong Repository)
            doctors = doctorRepository.findBySpecialty_SpecialtyId(specialtyId);
        } else if (query != null && !query.isEmpty()) {
            // Lọc theo Tên (Sẽ cần hàm findByUser_FirstNameContainingIgnoreCase... trong Repository)
            // Lấy từ khóa: tìm kiếm trong cả tên, mô tả
            doctors = doctorRepository.findByUser_FirstNameContainingIgnoreCaseOrUser_LastNameContainingIgnoreCaseOrProfileDescriptionContainingIgnoreCase(
                    query, query, query
            );
        } else {
            // Không có bộ lọc nào -> Trả về tất cả (logic cũ)
            doctors = doctorRepository.findAll();
        }

        return doctorMapper.toDoctorProfileResponseList(doctors);
    }
    public List<DoctorProfileResponse> getDoctorsByService(Integer serviceId){
        // 1. Tìm Dịch vụ (Service)
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Dịch vụ ID: " + serviceId));

        // 2. Lấy Specialty ID của Dịch vụ này
        Integer specialtyId = service.getSpecialty().getSpecialtyId();

        // 3. Tìm Bác sĩ thuộc Chuyên khoa đó (Sử dụng hàm Repository mới)
        List<Doctor> doctors = doctorRepository.findBySpecialty_SpecialtyId(specialtyId);

        // 4. Map và trả về
        return doctorMapper.toDoctorProfileResponseList(doctors);
    }

    public DoctorProfileResponse updateMyProfile(Integer doctorId, DoctorProfileUpdateRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ Bác sĩ"));

        String normalizedName = request.getFullName().trim().replaceAll("\\s+", " ");
        int splitAt = normalizedName.lastIndexOf(' ');
        if (splitAt > 0) {
            doctor.getUser().setLastName(normalizedName.substring(0, splitAt));
            doctor.getUser().setFirstName(normalizedName.substring(splitAt + 1));
        } else {
            doctor.getUser().setFirstName(normalizedName);
        }

        if (request.getEmail() != null) {
            doctor.getUser().setEmail(request.getEmail().trim());
        }
        if (request.getPhoneNumber() != null) {
            doctor.getUser().setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa"));
            doctor.setSpecialty(specialty);
        }
        if (request.getDegree() != null) {
            doctor.setDegree(request.getDegree().trim());
        }
        if (request.getYearsOfExperience() != null) {
            doctor.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getProfileDescription() != null) {
            doctor.setProfileDescription(request.getProfileDescription().trim());
        }

        return doctorMapper.toDoctorProfileResponse(doctorRepository.save(doctor));
    }

    public DoctorSettingsResponse getMySettings(Integer doctorId) {
        DoctorSettings settings = doctorSettingsRepository.findById(doctorId).orElse(null);
        if (settings == null) {
            return new DoctorSettingsResponse(Map.of(), Map.of());
        }
        return new DoctorSettingsResponse(readSettings(settings.getNotificationSettings()), readSettings(settings.getPreferences()));
    }

    public DoctorSettingsResponse updateMySettings(Integer doctorId, DoctorSettingsRequest request) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Không tìm thấy hồ sơ Bác sĩ");
        }
        DoctorSettings settings = doctorSettingsRepository.findById(doctorId).orElseGet(DoctorSettings::new);
        settings.setDoctorId(doctorId);
        settings.setNotificationSettings(writeSettings(request.notificationSettings()));
        settings.setPreferences(writeSettings(request.preferences()));
        DoctorSettings saved = doctorSettingsRepository.save(settings);
        return new DoctorSettingsResponse(readSettings(saved.getNotificationSettings()), readSettings(saved.getPreferences()));
    }

    private Map<String, Object> readSettings(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception exception) {
            throw new IllegalStateException("Dữ liệu cài đặt không hợp lệ", exception);
        }
    }

    private String writeSettings(Map<String, Object> settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Không thể lưu cài đặt", exception);
        }
    }
}
