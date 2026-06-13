package com.example.medbook.controller;

import com.example.medbook.entity.Otp;
import com.example.medbook.entity.User;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.repository.OtpRepository;
import com.example.medbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping({"/forgot-password", "/api/forgot-password"})
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/send-otp")
    @Transactional
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email không được để trống."));
        }

        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new MessageResponse("Email không tồn tại trong hệ thống."));
        }

        // Generate OTP
        int otpCodeInt = 100000 + new Random().nextInt(900000);
        String otpCode = String.valueOf(otpCodeInt);

        // Delete old OTP if exists
        otpRepository.deleteByEmail(email);

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtp(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(otp);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("MedBooking - Mã xác thực OTP");
            message.setText("Mã OTP của bạn là: " + otpCode + ". Mã có hiệu lực trong vòng 10 phút.");
            mailSender.send(message);
        } catch (Exception e) {
            // Log the OTP for testing if SMTP is not configured
            System.out.println("=================================================");
            System.out.println("LOGGED OTP FOR " + email + " IS: " + otpCode);
            System.out.println("=================================================");
            // Still return success in dev/test environment but log it, or return error as requested
            // Since "Không bỏ qua lỗi", let's return error but log the OTP so we know it happened
            return ResponseEntity.status(500).body(new MessageResponse("Lỗi gửi mail: " + e.getMessage()));
        }

        return ResponseEntity.ok(new MessageResponse("Mã OTP đã được gửi thành công!"));
    }

    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otpCode = request.get("otp");
        String password = request.get("password");

        if (email == null || otpCode == null || password == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Dữ liệu không hợp lệ."));
        }

        Optional<Otp> otpRecordOpt = otpRepository.findByEmailAndOtp(email, otpCode);
        if (otpRecordOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Mã OTP không chính xác."));
        }

        Otp otpRecord = otpRecordOpt.get();
        if (LocalDateTime.now().isAfter(otpRecord.getExpiresAt())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Mã OTP đã hết hạn."));
        }

        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new MessageResponse("Không tìm thấy tài khoản."));
        }

        User user = userOpt.get();
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);

        otpRepository.delete(otpRecord);

        return ResponseEntity.ok(new MessageResponse("Đổi mật khẩu thành công."));
    }
}
