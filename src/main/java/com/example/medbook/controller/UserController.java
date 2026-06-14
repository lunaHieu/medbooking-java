package com.example.medbook.controller;

import com.example.medbook.dto.request.UpdateProfileRequest;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.dto.response.UserProfileResponse;
import com.example.medbook.security.services.UserDetailsImpl;
import com.example.medbook.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping({"/user/profile", "/api/user/profile"})
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        UserProfileResponse response = userService.getMyProfile(currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping({"/user/profile", "/api/user/profile"})
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        UserProfileResponse response = userService.updateMyProfile(currentUser, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = {"/user/upload-avatar", "/api/user/upload-avatar"}, consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        UserProfileResponse response = userService.uploadAvatar(currentUser, file);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/user/change-password", "/api/user/change-password"})
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        
        userService.changePassword(currentUser, currentPassword, newPassword);
        return ResponseEntity.ok(new MessageResponse("Đổi mật khẩu thành công!"));
    }

    @GetMapping({"/user/family-members", "/api/user/family-members"})
    public ResponseEntity<List<Map<String, Object>>> getFamilyMembers(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(userService.getFamilyMembers(currentUser));
    }

    @PostMapping({"/user/family-members", "/api/user/family-members"})
    public ResponseEntity<?> addFamilyMember(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Integer relativeUserId = (Integer) request.get("RelativeUserID");
        String relationType = (String) request.get("RelationType");
        
        userService.addFamilyMember(currentUser, relativeUserId, relationType);
        return ResponseEntity.ok(new MessageResponse("Thêm thành viên thành công!"));
    }

    @DeleteMapping({"/user/family-members/{id}", "/api/user/family-members/{id}"})
    public ResponseEntity<?> removeFamilyMember(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        userService.removeFamilyMember(currentUser, id);
        return ResponseEntity.ok(new MessageResponse("Xóa thành viên khỏi danh sách."));
    }

    @GetMapping({"/users/search-public", "/api/users/search-public"})
    public ResponseEntity<List<Map<String, Object>>> searchUserPublic(
            @RequestParam("query") String query,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(userService.searchUserPublic(currentUser, query));
    }

    @Autowired
    private com.example.medbook.service.FeedbackService feedbackService;

    @PostMapping({"/appointments/{appointmentId}/feedback", "/api/appointments/{appointmentId}/feedback"})
    public ResponseEntity<?> submitFeedback(
            @PathVariable Integer appointmentId,
            @Valid @RequestBody com.example.medbook.dto.request.FeedbackRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        request.setAppointmentId(appointmentId);
        return feedbackService.submitFeedback(request, currentUser);
    }
}

