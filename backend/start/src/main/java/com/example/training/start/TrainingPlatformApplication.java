package com.example.training.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.training")
public class TrainingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainingPlatformApplication.class, args);
    }
}
