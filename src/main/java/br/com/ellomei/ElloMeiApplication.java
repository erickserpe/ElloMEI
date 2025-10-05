package br.com.ellomei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching // Enable Spring Boot caching functionality
@EnableAsync   // Enable asynchronous execution for better performance
public class ElloMeiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElloMeiApplication.class, args);
    }

}
