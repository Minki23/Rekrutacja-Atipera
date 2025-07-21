package com.example.rekrutacja_atipera.controller;

import com.example.rekrutacja_atipera.model.Branch;
import com.example.rekrutacja_atipera.model.GithubApiException;
import com.example.rekrutacja_atipera.model.Repository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Value("${github.token}")
    private static String githubToken;
    private static final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
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
                                        extractMessageFromGithubError(body)
                                )))
                )
                .bodyToMono(new ParameterizedTypeReference<List<Repository>>() {})
                .flatMapMany(repos -> Flux.fromIterable(filterForks(repos)))
                .flatMap(repo ->
                        addBranches(repo, username).thenReturn(repo)
                )
                .collectList()
                .map(filteredRepos -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(filteredRepos)
                );
    }

    private String extractMessageFromGithubError(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            return root.has("message") ? root.get("message").asText() : "Unknown error";
        } catch (Exception e) {
            return "Invalid error response";
        }
    }


    private List<Repository> filterForks(List<Repository> repositories) {
        return repositories.stream()
                .filter(repository -> !repository.isFork())
                .collect(Collectors.toList());
    }

    public Mono<Void> addBranches(Repository repository, String username) {
        return webClient.get()
                .uri("/repos/{username}/{repository}/branches", username, repository.getName())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new GithubApiException(
                                        response.statusCode(),
                                        extractMessageFromGithubError(body)
                                )))
                )
                .bodyToMono(new ParameterizedTypeReference<List<Branch>>() {})
                .doOnNext(repository::setBranches)
                .then();
    }
}
