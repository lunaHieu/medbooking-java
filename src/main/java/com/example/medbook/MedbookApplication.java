package com.example.medbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedbookApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedbookApplication.class, args);
	}

}
