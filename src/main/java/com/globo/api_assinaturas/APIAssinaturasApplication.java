package com.globo.api_assinaturas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.globo.api_assinaturas.config.RenewalProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(RenewalProperties.class)
public class APIAssinaturasApplication {

	public static void main(String[] args) {
		SpringApplication.run(APIAssinaturasApplication.class, args);
	}
}