package com.example.medbook.service;

import com.example.medbook.dto.response.DoctorProfileResponse;
import com.example.medbook.entity.Doctor;
import com.example.medbook.entity.Service;
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.mapper.DoctorMapper;
import com.example.medbook.repository.DoctorRepository;
import com.example.medbook.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@org.springframework.stereotype.Service
public class DoctorService {
    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private ServiceRepository serviceRepository;
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
}
