package com.example.medbook.repository;

import com.example.medbook.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Integer> {

    void deleteAllByUser_UserId(Integer userId);
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.userId = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    List<Notification> findByUser_UserIdOrderBySentAtDesc(Integer userId);
    List<Notification> findAllByOrderBySentAtDesc();

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.userId = :userId AND n.status = 'Read'")
    void deleteReadByUserId(@Param("userId") Integer userId);
}

