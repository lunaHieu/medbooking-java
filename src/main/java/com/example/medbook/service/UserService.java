package com.example.medbook.service;

import com.example.medbook.dto.request.UpdateProfileRequest;
import com.example.medbook.dto.response.UserProfileResponse;
import com.example.medbook.entity.User;
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.mapper.UserMapper;
import com.example.medbook.repository.UserRepository;
import com.example.medbook.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    // 1. Lấy thông tin Profile
    public UserProfileResponse getMyProfile(UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        return userMapper.toUserProfileResponse(user);
    }

    // 2. Cập nhật thông tin Profile
    @Transactional
    public UserProfileResponse updateMyProfile(UserDetailsImpl currentUser, UpdateProfileRequest request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        // Dùng Mapper để cập nhật thông tin
        userMapper.updateUserFromRequest(request, user);

        User updatedUser = userRepository.save(user);
        return userMapper.toUserProfileResponse(updatedUser);
    }
}