package com.example.medbook.mapper;

import com.example.medbook.dto.request.ServiceRequest;
import com.example.medbook.dto.response.ServiceResponse;
import com.example.medbook.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServiceMapper {
    //Chuyển Entity -> Response DTO
    @Mapping(target = "specialtyName", source = "specialty.specialtyName")
    ServiceResponse toServiceResponse(Service service);
    //Chuyển List<Entity> -> List<Response DTO>
    List<ServiceResponse> toServiceResponseList(List<Service> services);
    //Request DTO -> Entity
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "specialty",ignore = true)
    Service toService(ServiceRequest request);

    //Cập nhật Entity từ Request DTO
    @Mapping(target = "specialty", ignore = true)
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "price", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateServiceFromRequest(ServiceRequest request, @MappingTarget Service service);
}
