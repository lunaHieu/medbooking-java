package com.example.medbook.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataMigrationRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        jdbcTemplate.update("UPDATE users SET Role = 'ADMIN' WHERE Role = 'ROLE_ADMIN'");
        jdbcTemplate.update("UPDATE users SET Role = 'DOCTOR' WHERE Role = 'ROLE_DOCTOR'");
        jdbcTemplate.update("UPDATE users SET Role = 'MEDICAL_STAFF' WHERE Role = 'ROLE_STAFF'");
        jdbcTemplate.update("UPDATE users SET Role = 'PATIENT' WHERE Role = 'ROLE_PATIENT'");
    }
}
