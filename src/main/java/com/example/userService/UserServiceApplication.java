package com.example.userService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.cache.annotation.EnableCaching;

@EnableJpaAuditing
@SpringBootApplication
@EnableCaching
public class UserServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}
