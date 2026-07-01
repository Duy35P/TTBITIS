package com.bitis.luckydraw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LuckyDrawApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuckyDrawApplication.class, args);
    }
}
