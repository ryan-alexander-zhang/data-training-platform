package com.example.training.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = "com.example.training")
@MapperScan("com.example.training.infra.mapper")
public class TrainingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainingPlatformApplication.class, args);
    }
}
