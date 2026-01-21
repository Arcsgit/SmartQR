package com.example.smartqr;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartqrApplication {
    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
            );
            System.out.println("Environment variables loaded from .env file");
        } catch (Exception e) {
            System.out.println("No .env file found");
        }
        SpringApplication.run(SmartqrApplication.class, args);
    }
}
