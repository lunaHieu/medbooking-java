package com.example.medbook.controller;

import com.example.medbook.dto.request.ForgotPasswordRequest;
import com.example.medbook.dto.request.ResetPasswordRequest;
import com.example.medbook.entity.Otp;
import com.example.medbook.entity.User;
import com.example.medbook.repository.OtpRepository;
import com.example.medbook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpRepository otpRepository;
    @Mock private JavaMailSender mailSender;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ForgotPasswordController controller;

    @Test
    void doesNotRevealWhetherEmailExistsWhenSendingOtp() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@example.com");
        when(otpRepository.findFirstByEmailIgnoreCaseOrderByExpiresAtDesc(request.getEmail()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.empty());

        var response = controller.sendOtp(request);

        assertEquals(200, response.getStatusCode().value());
        verify(otpRepository, never()).save(any());
        verify(mailSender, never()).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void storesOnlyHashedOtpForExistingAccount() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("patient@example.com");
        when(otpRepository.findFirstByEmailIgnoreCaseOrderByExpiresAtDesc(request.getEmail()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(new User()));
        when(passwordEncoder.encode(anyString())).thenReturn("bcrypt-otp");

        var response = controller.sendOtp(request);

        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository).save(otpCaptor.capture());
        assertEquals("bcrypt-otp", otpCaptor.getValue().getOtp());
        assertEquals(0, otpCaptor.getValue().getFailedAttempts());
        assertEquals(200, response.getStatusCode().value());
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void incrementsFailedAttemptsForInvalidOtp() {
        Otp otp = new Otp();
        otp.setEmail("patient@example.com");
        otp.setOtp("bcrypt-otp");
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otp.setLastSentAt(LocalDateTime.now());

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(otp.getEmail());
        request.setOtp("000000");
        request.setPassword("new-password");
        when(otpRepository.findFirstByEmailIgnoreCaseOrderByExpiresAtDesc(otp.getEmail()))
                .thenReturn(Optional.of(otp));
        when(passwordEncoder.matches(request.getOtp(), otp.getOtp())).thenReturn(false);

        var response = controller.resetPassword(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(1, otp.getFailedAttempts());
        verify(otpRepository).save(otp);
        verify(userRepository, never()).save(any());
    }
}
