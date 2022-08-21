package com.example.cemozan.bankingsystemproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages= {"JWT","Security","com.example.cemozan.bankingsystemproject"})
public class BankingsystemprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingsystemprojectApplication.class, args);
	}

}
