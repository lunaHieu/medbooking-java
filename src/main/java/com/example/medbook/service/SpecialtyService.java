package com.example.medbook.service;

import com.example.medbook.dto.request.SpecialtyRequest;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.dto.response.SpecialtyResponse;
import com.example.medbook.entity.Specialty; // <-- Entity
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.mapper.SpecialtyMapper; // <-- Mapper
import com.example.medbook.repository.SpecialtyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service; // <-- PHẢI CÓ DÒNG NÀY

import java.util.List;

@Service // <-- ĐẢM BẢO BẠN CÓ DÒNG NÀY
public class SpecialtyService {

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private SpecialtyMapper specialtyMapper; // <-- Bạn phải tiêm (inject) Mapper

    // --- 1. TẠO MỚI (Cho Admin) ---
    public SpecialtyResponse createSpecialty(SpecialtyRequest request) {
        if (specialtyRepository.findBySpecialtyNameContainingIgnoreCase(request.getSpecialtyName()).isPresent()) {
            throw new IllegalArgumentException("Tên chuyên khoa đã tồn tại");
        }

        // --- SỬA LỖI Ở ĐÂY ---
        // 1. Dùng Mapper chuyển DTO (request) -> Entity (specialty)
        Specialty specialty = specialtyMapper.toSpecialty(request);

        // 2. Lưu Entity (specialty) vào CSDL
        Specialty savedSpecialty = specialtyRepository.save(specialty);
        // --- KẾT THÚC SỬA ---

        // 3. Dùng Mapper chuyển Entity -> Response DTO để trả về
        return specialtyMapper.toSpecialtyResponse(savedSpecialty);
    }

    // --- 2. LẤY TẤT CẢ (Cho Public) ---
    public List<SpecialtyResponse> getAllSpecialties() {
        List<Specialty> specialties = specialtyRepository.findAll();
        return specialtyMapper.toSpecialtyResponseList(specialties);
    }

    // --- 3. LẤY 1 CÁI (Cho Public) ---
    public SpecialtyResponse getSpecialtyById(Integer id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa với ID: " + id));
        return specialtyMapper.toSpecialtyResponse(specialty);
    }

    // --- 4. CẬP NHẬT (Cho Admin) ---
    public SpecialtyResponse updateSpecialty(Integer id, SpecialtyRequest request) {
        Specialty existingSpecialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa với ID: " + id));

        // (Kiểm tra tên trùng)
        if (request.getSpecialtyName() != null && !request.getSpecialtyName().equals(existingSpecialty.getSpecialtyName())) {
            if (specialtyRepository.findBySpecialtyNameContainingIgnoreCase(request.getSpecialtyName()).isPresent()) {
                throw new IllegalArgumentException("Tên chuyên khoa đã tồn tại");
            }
        }

        // Dùng Mapper để cập nhật (không phải 'save(request)')
        specialtyMapper.updateSpecialtyFromRequest(request, existingSpecialty);

        Specialty updatedSpecialty = specialtyRepository.save(existingSpecialty); // Lưu Entity
        return specialtyMapper.toSpecialtyResponse(updatedSpecialty);
    }

    // --- 5. XÓA (Cho Admin) ---
    public ResponseEntity<MessageResponse> deleteSpecialty(Integer id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa với ID: " + id));

        specialtyRepository.delete(specialty);
        return ResponseEntity.ok(new MessageResponse("Đã xóa chuyên khoa thành công!"));
    }
}