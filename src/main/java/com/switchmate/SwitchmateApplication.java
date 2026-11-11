package com.switchmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SwitchmateApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwitchmateApplication.class, args);
	}
}