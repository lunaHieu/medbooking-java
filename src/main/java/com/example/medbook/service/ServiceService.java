package com.example.medbook.service;

import com.example.medbook.dto.request.ServiceRequest;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.dto.response.ServiceResponse;
import com.example.medbook.entity.Service;
import com.example.medbook.entity.Specialty;
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.mapper.ServiceMapper;
import com.example.medbook.repository.DoctorRepository;
import com.example.medbook.repository.ServiceRepository;
import com.example.medbook.repository.SpecialtyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.example.medbook.entity.Doctor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceService {
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private ServiceMapper serviceMapper;

    @Autowired
    private DoctorRepository doctorRepository;
    public ServiceResponse createService(ServiceRequest request) {
        if(serviceRepository.findByServiceName(request.getServiceName()).isPresent()){
            throw new IllegalArgumentException("Tên dịch vụ đã tồn tại.");
        }
        //Kiểm tra khóa ngoại SpecialtyID
        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Chuyên khoa ID: " + request.getSpecialtyId()));
        //Chuyển đổi DTO -> Entity
        Service service = serviceMapper.toService(request);

        //Gán khóa ngoại FK
        service.setSpecialty(specialty);
        Service savedService = serviceRepository.save(service);
        return serviceMapper.toServiceResponse(savedService);
    }

    public List<ServiceResponse> getAllServices() {
        List<Service> services = serviceRepository.findAll();
        List<ServiceResponse> responseList = new ArrayList<>();

        for (Service service : services) {
            //Map sang DTO
            ServiceResponse dto;
            try {
                dto = serviceMapper.toServiceResponse(service);
            } catch (Exception e) {
                dto = new ServiceResponse();
                dto.setServiceId(service.getServiceId());
                dto.setServiceName(service.getServiceName());
                dto.setDescription(service.getDescription());
                dto.setEstimatedDuration(service.getEstimatedDuration());
                dto.setPrice(service.getPrice());
                dto.setImageURL(service.getImageURL());
                dto.setSpecialtyName("Chưa rõ");
            }

            //TÌM BÁC SĨ NỔI BẬT (Logic: Lấy bác sĩ đầu tiên của chuyên khoa này)
            Integer specialtyId = null;
            try {
                if (service.getSpecialty() != null) {
                    specialtyId = service.getSpecialty().getSpecialtyId();
                }
            } catch (Exception e) {
                // Bỏ qua nếu specialty bị lỗi / không tồn tại trong DB (mismatch)
            }

            if (specialtyId != null) {
                try {
                    List<Doctor> doctors = doctorRepository.findBySpecialty_SpecialtyId(specialtyId);

                    if (doctors != null && !doctors.isEmpty()) {
                        // Lấy bác sĩ đầu tiên làm "Gương mặt đại diện"
                        Doctor featuredDoc = doctors.get(0);
                        if (featuredDoc.getUser() != null) {
                            String docName = "BS. " + 
                                    (featuredDoc.getUser().getLastName() != null ? featuredDoc.getUser().getLastName() : "") + " " + 
                                    (featuredDoc.getUser().getFirstName() != null ? featuredDoc.getUser().getFirstName() : "");
                            dto.setFeaturedDoctorName(docName.trim());
                        } else {
                            dto.setFeaturedDoctorName("Đang cập nhật");
                        }
                        dto.setFeaturedDoctorImage(featuredDoc.getImageURL());
                    } else {
                        dto.setFeaturedDoctorName("Đang cập nhật");
                    }
                } catch (Exception e) {
                    dto.setFeaturedDoctorName("Đang cập nhật");
                }
            } else {
                dto.setFeaturedDoctorName("Đang cập nhật");
            }

            responseList.add(dto);
        }

        return responseList;
    }
    public ServiceResponse getServiceById(Integer id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Dịch vụ ID: " + id));

        ServiceResponse dto;
        try {
            dto = serviceMapper.toServiceResponse(service);
        } catch (Exception e) {
            dto = new ServiceResponse();
            dto.setServiceId(service.getServiceId());
            dto.setServiceName(service.getServiceName());
            dto.setDescription(service.getDescription());
            dto.setEstimatedDuration(service.getEstimatedDuration());
            dto.setPrice(service.getPrice());
            dto.setImageURL(service.getImageURL());
            dto.setSpecialtyName("Chưa rõ");
        }

        // Tìm bác sĩ nổi bật
        Integer specialtyId = null;
        try {
            if (service.getSpecialty() != null) {
                specialtyId = service.getSpecialty().getSpecialtyId();
            }
        } catch (Exception e) {
            // Bỏ qua
        }

        if (specialtyId != null) {
            try {
                List<Doctor> doctors = doctorRepository.findBySpecialty_SpecialtyId(specialtyId);

                if (doctors != null && !doctors.isEmpty()) {
                    Doctor featuredDoc = doctors.get(0);
                    if (featuredDoc.getUser() != null) {
                        String docName = "BS. " + 
                                (featuredDoc.getUser().getLastName() != null ? featuredDoc.getUser().getLastName() : "") + " " + 
                                (featuredDoc.getUser().getFirstName() != null ? featuredDoc.getUser().getFirstName() : "");
                        dto.setFeaturedDoctorName(docName.trim());
                    } else {
                        dto.setFeaturedDoctorName("Đang cập nhật");
                    }
                    dto.setFeaturedDoctorImage(featuredDoc.getImageURL());
                } else {
                    dto.setFeaturedDoctorName("Đang cập nhật");
                }
            } catch (Exception e) {
                dto.setFeaturedDoctorName("Đang cập nhật");
            }
        } else {
            dto.setFeaturedDoctorName("Đang cập nhật");
        }

        return dto;
    }
    public ServiceResponse updateService(Integer id, ServiceRequest request) {
        Service existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ ID: " + id));
        if(request.getSpecialtyId() != null){
            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Chuyên khoa ID: " + request.getSpecialtyId()));
            existingService.setSpecialty(specialty);
        }
        serviceMapper.updateServiceFromRequest(request, existingService);
        Service updatedService = serviceRepository.save(existingService);
        return serviceMapper.toServiceResponse(updatedService);
    }
    public ResponseEntity<MessageResponse> deleteService(Integer id) {
        if(!serviceRepository.existsById(id)){
            throw new ResourceNotFoundException("Không tìm thấy dịch vụ ID: " + id);
        }
        serviceRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Đã xóa dịch vụ thành công!"));
    }
}
