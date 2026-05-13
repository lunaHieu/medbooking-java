package com.example.medbook.repository;

import com.example.medbook.entity.Feedbacks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedbacks, Integer> {

    Optional<Feedbacks> findByAppointment_AppointmentId(Integer appointmentId);

    @Query("SELECT f FROM Feedbacks f WHERE f.targetType = 'Doctor' AND f.targetId = :doctorId")
    List<Feedbacks> findByDoctorId(Integer doctorId);

    @Query("SELECT AVG(f.rating) FROM Feedbacks f WHERE f.targetType = 'Doctor' AND f.targetId = :doctorId")
    Double findAverageRatingByDoctorId(Integer doctorId);

    List<Feedbacks> findAllByOrderByCreatedAtDesc();
    //Tìm đánh giá theo loại
    List<Feedbacks> findByTargetTypeOrderByCreatedAtDesc(String targetType);
    // Lấy top 5 hoặc 10 đánh giá có Rating >= 4, sắp xếp mới nhất
    List<Feedbacks> findTop10ByRatingGreaterThanEqualOrderByCreatedAtDesc(Integer rating);
}