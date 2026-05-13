package com.example.medbook.mapper;

import com.example.medbook.dto.request.SpecialtyRequest;
import com.example.medbook.dto.response.SpecialtyResponse;
import com.example.medbook.entity.Specialty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {

    // 1. Chuyển từ Entity -> Response DTO (để trả về client)
    SpecialtyResponse toSpecialtyResponse(Specialty specialty);

    // 2. Chuyển từ List<Entity> -> List<Response DTO>
    List<SpecialtyResponse> toSpecialtyResponseList(List<Specialty> specialties);

    // 3. Chuyển từ Request DTO -> Entity (dùng khi TẠO MỚI)
    @Mapping(target = "specialtyId", ignore = true) // Bỏ qua ID khi tạo
    Specialty toSpecialty(SpecialtyRequest request);

    // 4. Cập nhật Entity từ Request DTO (dùng khi CẬP NHẬT)
    @Mapping(target = "specialtyId", ignore = true)
    // Bỏ qua các trường null (ví dụ: nếu client không gửi description,
    // thì không ghi đè description cũ thành null)
    @Mapping(target = "description", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "imageURL", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSpecialtyFromRequest(SpecialtyRequest request, @MappingTarget Specialty specialty);
}