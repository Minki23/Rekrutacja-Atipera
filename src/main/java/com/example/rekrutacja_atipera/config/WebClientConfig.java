package com.example.rekrutacja_atipera.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient gitHubWebClient(@Value("${github.token:}") String token) {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl("https://api.github.com");

        if (token != null && !token.isEmpty()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }

        return builder.build();
    }
}