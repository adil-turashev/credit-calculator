package org.example.credit4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Credit4Application {
    public static void main(String[] args) {
        SpringApplication.run(Credit4Application.class, args);
    }
}