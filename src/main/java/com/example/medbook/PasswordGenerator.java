package com.example.medbook;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 2. Mật khẩu gốc bạn muốn mã hóa
        String rawPassword = "12345678";

        // 3. Mã hóa
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // 4. In kết quả ra màn hình Console
        System.out.println("Mật khẩu gốc: " + rawPassword);
        System.out.println("Mật khẩu đã Hash (BCrypt): " + hashedPassword);
    }
}
