package com.example.medbook.service;

import com.example.medbook.dto.request.UpdateProfileRequest;
import com.example.medbook.dto.response.UserProfileResponse;
import com.example.medbook.entity.User;
import com.example.medbook.entity.UserRelation;
import com.example.medbook.entity.Otp;
import com.example.medbook.exception.ResourceNotFoundException;
import com.example.medbook.mapper.UserMapper;
import com.example.medbook.repository.UserRepository;
import com.example.medbook.repository.UserRelationRepository;
import com.example.medbook.repository.OtpRepository;
import com.example.medbook.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRelationRepository userRelationRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Transactional
    public UserProfileResponse uploadAvatar(UserDetailsImpl currentUser, MultipartFile file) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file tải lên không được để trống.");
        }

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("src/main/resources/uploads/avatars").toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            String imagePath = "avatars/" + fileName;
            user.setAvatarURL(imagePath);
            User saved = userRepository.save(user);
            return userMapper.toUserProfileResponse(saved);
        } catch (IOException e) {
            throw new RuntimeException("Could not store avatar. Error: " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean changePassword(UserDetailsImpl currentUser, String currentPassword, String newPassword) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public List<Map<String, Object>> getFamilyMembers(UserDetailsImpl currentUser) {
        List<UserRelation> relations = userRelationRepository.findByUser_UserId(currentUser.getId());
        return relations.stream().map(r -> {
            User m = r.getRelativeUser();
            Map<String, Object> map = new HashMap<>();
            map.put("UserID", m.getUserId());
            String fullName = "";
            if (m.getLastName() != null && m.getFirstName() != null) {
                fullName = m.getLastName() + " " + m.getFirstName();
            } else if (m.getFirstName() != null) {
                fullName = m.getFirstName();
            }
            map.put("FullName", fullName);
            map.put("PhoneNumber", m.getPhoneNumber());
            map.put("Email", m.getEmail());
            map.put("DateOfBirth", m.getDateOfBirth() != null ? m.getDateOfBirth().toString() : null);
            map.put("Gender", m.getGender());
            map.put("avatar_url", m.getAvatarURL());
            map.put("RelationType", r.getRelationType());
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void addFamilyMember(UserDetailsImpl currentUser, Integer relativeUserId, String relationType) {
        if (currentUser.getId().equals(relativeUserId)) {
            throw new IllegalArgumentException("Bạn không thể thêm chính mình vào gia đình.");
        }

        Optional<UserRelation> existing = userRelationRepository.findByUser_UserIdAndRelativeUser_UserId(currentUser.getId(), relativeUserId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Người này đã có trong danh sách.");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        User relative = userRepository.findById(relativeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người thân với ID này."));

        UserRelation relation = new UserRelation();
        relation.setUser(user);
        relation.setRelativeUser(relative);
        relation.setRelationType(relationType);
        userRelationRepository.save(relation);
    }

    @Transactional
    public void removeFamilyMember(UserDetailsImpl currentUser, Integer relativeUserId) {
        UserRelation relation = userRelationRepository.findByUser_UserIdAndRelativeUser_UserId(currentUser.getId(), relativeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mối liên kết thành viên gia đình này."));
        userRelationRepository.delete(relation);
    }

    public List<Map<String, Object>> searchUserPublic(UserDetailsImpl currentUser, String query) {
        if (query == null || query.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !u.getUserId().equals(currentUser.getId()))
                .filter(u -> query.equalsIgnoreCase(u.getPhoneNumber()) || query.equalsIgnoreCase(u.getEmail()))
                .collect(Collectors.toList());
                
        return users.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("UserID", m.getUserId());
            String fullName = "";
            if (m.getLastName() != null && m.getFirstName() != null) {
                fullName = m.getLastName() + " " + m.getFirstName();
            } else if (m.getFirstName() != null) {
                fullName = m.getFirstName();
            }
            map.put("FullName", fullName);
            map.put("PhoneNumber", m.getPhoneNumber());
            map.put("Email", m.getEmail());
            map.put("avatar_url", m.getAvatarURL());
            map.put("Role", m.getRole());
            return map;
        }).collect(Collectors.toList());
    }
}