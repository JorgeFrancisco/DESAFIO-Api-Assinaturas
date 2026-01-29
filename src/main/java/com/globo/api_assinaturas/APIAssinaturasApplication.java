package com.globo.api_assinaturas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class APIAssinaturasApplication {

	public static void main(String[] args) {
		SpringApplication.run(APIAssinaturasApplication.class, args);
	}

}