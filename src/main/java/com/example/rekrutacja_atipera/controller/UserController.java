package com.example.rekrutacja_atipera.controller;

import com.example.rekrutacja_atipera.model.GithubApiException;
import com.example.rekrutacja_atipera.model.Repository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.View;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
public class UserController {

    private static final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.github.com")
            .build();
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final ObjectMapper objectMapper;

    public UserController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RequestMapping("/")
    public Mono<ResponseEntity<Object>> getUser(@RequestParam("username") String username) {
        return webClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new GithubApiException(
                                        response.statusCode(),
                                        "Client error: " + extractMessageFromGithubError(body)
                                )))
                )
                .bodyToMono(Object.class)
                .flatMap(body -> {
                    Object repositories = objectMapper.convertValue(body, Repository[].class);
                    return Mono.just(ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(repositories));
                })
                .onErrorResume(GithubApiException.class, ex -> {
                    logger.warn("GitHub API error: {}", ex.getMessage());

                    Map<String, Object> errorResponse = new LinkedHashMap<>();
                    errorResponse.put("status", ex.getStatus().value());
                    errorResponse.put("message", ex.getBody());

                    return Mono.just(ResponseEntity.status(ex.getStatus())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(errorResponse));
                });
    }

    private String extractMessageFromGithubError(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            return root.has("message") ? root.get("message").asText() : "Unknown error";
        } catch (Exception e) {
            return "Invalid error response";
        }
    }


    private Object filterForks(Object repoList) {
        return repoList;
    }
}
