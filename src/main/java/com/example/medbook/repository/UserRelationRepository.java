package com.example.medbook.repository;

import com.example.medbook.entity.UserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRelationRepository extends JpaRepository<UserRelation, Long> {
    List<UserRelation> findByUser_UserId(Integer userId);
    Optional<UserRelation> findByUser_UserIdAndRelativeUser_UserId(Integer userId, Integer relativeUserId);

    @Modifying
    @Query("DELETE FROM UserRelation ur WHERE ur.user.userId = :userId OR ur.relativeUser.userId = :userId")
    void deleteByUserOrRelativeUser(@Param("userId") Integer userId);
}

