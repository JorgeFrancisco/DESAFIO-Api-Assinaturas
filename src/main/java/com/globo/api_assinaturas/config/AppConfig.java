package com.globo.api_assinaturas.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RenewalProperties.class)
public class AppConfig {
}