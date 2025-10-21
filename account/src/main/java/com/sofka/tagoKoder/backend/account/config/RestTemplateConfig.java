package com.sofka.tagoKoder.backend.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate resTemplate(){
        return new RestTemplate();
    }
    
}
