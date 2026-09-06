package com.example.medbook.controller;

import com.example.medbook.entity.Otp;
import com.example.medbook.entity.User;
import com.example.medbook.dto.request.ForgotPasswordRequest;
import com.example.medbook.dto.request.ResetPasswordRequest;
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
import java.util.Optional;
import java.security.SecureRandom;

@RestController
@RequestMapping({"/forgot-password", "/api/forgot-password"})
public class ForgotPasswordController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final String OTP_SENT_MESSAGE = "Nếu email tồn tại, mã xác thực đã được gửi.";

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
    public ResponseEntity<MessageResponse> sendOtp(@jakarta.validation.Valid @RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail().trim();
        Optional<Otp> existingOtp = otpRepository.findFirstByEmailIgnoreCaseOrderByExpiresAtDesc(email);

        if (existingOtp.isPresent()
                && existingOtp.get().getLastSentAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now())) {
            return ResponseEntity.ok(new MessageResponse(OTP_SENT_MESSAGE));
        }

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse(OTP_SENT_MESSAGE));
        }

        int otpCodeInt = 100000 + SECURE_RANDOM.nextInt(900000);
        String otpCode = String.valueOf(otpCodeInt);

        Otp otp = existingOtp.orElseGet(Otp::new);
        otp.setEmail(email);
        otp.setOtp(passwordEncoder.encode(otpCode));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setLastSentAt(LocalDateTime.now());
        otp.setFailedAttempts(0);
        otpRepository.save(otp);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("MedBooking - Mã xác thực OTP");
            message.setText("Mã OTP của bạn là: " + otpCode + ". Mã có hiệu lực trong vòng 10 phút.");
            mailSender.send(message);
        } catch (Exception e) {
            otpRepository.delete(otp);
            return ResponseEntity.status(503).body(new MessageResponse("Không thể gửi mã xác thực. Vui lòng thử lại sau."));
        }

        return ResponseEntity.ok(new MessageResponse(OTP_SENT_MESSAGE));
    }

    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<?> resetPassword(@jakarta.validation.Valid @RequestBody ResetPasswordRequest request) {
        String email = request.getEmail().trim();
        Optional<Otp> otpRecordOpt = otpRepository.findFirstByEmailIgnoreCaseOrderByExpiresAtDesc(email);
        if (otpRecordOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Mã xác thực không hợp lệ hoặc đã hết hạn."));
        }

        Otp otpRecord = otpRecordOpt.get();
        if (LocalDateTime.now().isAfter(otpRecord.getExpiresAt())) {
            otpRepository.delete(otpRecord);
            return ResponseEntity.badRequest().body(new MessageResponse("Mã xác thực không hợp lệ hoặc đã hết hạn."));
        }

        if (!passwordEncoder.matches(request.getOtp(), otpRecord.getOtp())) {
            otpRecord.setFailedAttempts(otpRecord.getFailedAttempts() + 1);
            if (otpRecord.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                otpRepository.delete(otpRecord);
            } else {
                otpRepository.save(otpRecord);
            }
            return ResponseEntity.badRequest().body(new MessageResponse("Mã xác thực không hợp lệ hoặc đã hết hạn."));
        }

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Mã xác thực không hợp lệ hoặc đã hết hạn."));
        }

        User user = userOpt.get();
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        otpRepository.delete(otpRecord);

        return ResponseEntity.ok(new MessageResponse("Đổi mật khẩu thành công."));
    }
}
