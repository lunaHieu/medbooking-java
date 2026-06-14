package com.example.medbook.repository;

import com.example.medbook.entity.Service;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {

    @Override
    @EntityGraph(attributePaths = {"specialty"})
    List<Service> findAll();

    //Kiểm tra trùng lặp theo tên dịch vụ
    Optional<Service> findByServiceName(String serviceName);

    //Hàm tìm kiếm dịch vụ theo SpecialtyID
    List<Service> findBySpecialty_SpecialtyId(Integer specialtyId);
}
