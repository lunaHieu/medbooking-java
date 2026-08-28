package com.example.medbook;

import com.example.medbook.security.jwt.AuthTokenFilter;
import com.example.medbook.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- CẤU HÌNH CORS TỐI ƯU ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")).stream()
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Quan trọng: Cho phép Preflight
                .requestMatchers("/auth/**", "/api/auth/**").permitAll()
                .requestMatchers("/forgot-password/**", "/api/forgot-password/**").permitAll()
                .requestMatchers("/public/**", "/api/public/**").permitAll()
                .requestMatchers("/services/**", "/api/services/**").permitAll()
                .requestMatchers("/storage/**", "/api/storage/**").permitAll()
                .requestMatchers("/system-feedback", "/api/system-feedback").permitAll()
                .requestMatchers("/error", "/api/error").permitAll() // Cho phép hiển thị lỗi thật
                .requestMatchers("/admin/**", "/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "QuanTriVien", "ADMIN", "ROLE_QUANTRIVIEN", "ROLE_ROLE_ADMIN", "NhanVien", "STAFF", "ROLE_NHANVIEN", "ROLE_STAFF")
                .requestMatchers("/doctor/**", "/api/doctor/**").hasAnyAuthority("ROLE_DOCTOR", "BacSi", "DOCTOR", "ROLE_BACSI")
                .requestMatchers("/staff/**", "/api/staff/**").hasAnyAuthority("ROLE_MEDICAL_STAFF", "NhanVien", "STAFF", "ROLE_NHANVIEN", "ROLE_STAFF", "ROLE_ADMIN", "QuanTriVien", "ADMIN", "ROLE_QUANTRIVIEN")
                .requestMatchers("/patient/**", "/api/patient/**").hasAnyAuthority("ROLE_PATIENT", "BenhNhan", "PATIENT", "ROLE_BENHNHAN", "ROLE_ROLE_PATIENT")
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
