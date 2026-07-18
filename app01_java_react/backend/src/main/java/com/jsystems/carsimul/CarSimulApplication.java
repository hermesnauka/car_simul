package com.jsystems.carsimul;

import com.jsystems.carsimul.config.ExamRulesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Clock;

import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(ExamRulesProperties.class)
public class CarSimulApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarSimulApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
