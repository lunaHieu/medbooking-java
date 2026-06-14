package com.example.medbook.repository;

import com.example.medbook.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor,Integer> {

    @Override
    @Query("SELECT d FROM Doctor d JOIN FETCH d.user JOIN FETCH d.specialty")
    List<Doctor> findAll();

    @Query("SELECT d FROM Doctor d JOIN FETCH d.user JOIN FETCH d.specialty WHERE d.specialty.specialtyId = :specialtyId")
    List<Doctor> findBySpecialty_SpecialtyId(@Param("specialtyId") Integer specialtyId);

    @Query("SELECT d FROM Doctor d JOIN FETCH d.user JOIN FETCH d.specialty WHERE " +
           "LOWER(d.user.firstName) LIKE LOWER(CONCAT('%', :firstNameQuery, '%')) OR " +
           "LOWER(d.user.lastName) LIKE LOWER(CONCAT('%', :lastNameQuery, '%')) OR " +
           "LOWER(d.profileDescription) LIKE LOWER(CONCAT('%', :descriptionQuery, '%'))")
    List<Doctor> findByUser_FirstNameContainingIgnoreCaseOrUser_LastNameContainingIgnoreCaseOrProfileDescriptionContainingIgnoreCase(
            @Param("firstNameQuery") String firstNameQuery,
            @Param("lastNameQuery") String lastNameQuery,
            @Param("descriptionQuery") String descriptionQuery);
}

