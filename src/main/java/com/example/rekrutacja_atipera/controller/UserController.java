package com.example.rekrutacja_atipera.controller;

import com.example.rekrutacja_atipera.model.Branch;
import com.example.rekrutacja_atipera.model.dto.BranchDTO;
import com.example.rekrutacja_atipera.model.dto.RepositoryDTO;
import com.example.rekrutacja_atipera.exception.GithubApiException;
import com.example.rekrutacja_atipera.model.Repository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
@RestController
public class UserController {

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public UserController(ObjectMapper objectMapper, WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @RequestMapping("/")
    public Mono<ResponseEntity<Object>> getUser(@RequestParam("username") String username) {
        return fetchUserRepositories(username)
                .flatMapMany(repos -> Flux.fromIterable(filterForks(repos)))
                .flatMap(repo -> enrichWithBranches(repo, username))
                .collectList()
                .map(this::mapToResponseEntity);
    }

    private Mono<List<Repository>> fetchUserRepositories(String username) {
        return webClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleGithubApiError)
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

    private Mono<Repository> enrichWithBranches(Repository repository, String username) {
        return fetchBranchesForRepository(username, repository.getName())
                .doOnNext(repository::setBranches)
                .thenReturn(repository);
    }

    private Mono<List<Branch>> fetchBranchesForRepository(String username, String repoName) {
        return webClient.get()
                .uri("/repos/{username}/{repo}/branches", username, repoName)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleGithubApiError)
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

    private List<Repository> filterForks(List<Repository> repositories) {
        return repositories.stream()
                .filter(repo -> !repo.isFork())
                .collect(Collectors.toList());
    }

    private ResponseEntity<Object> mapToResponseEntity(List<Repository> repositories) {
        List<RepositoryDTO> responseList = repositories.stream()
                .map(repo -> new RepositoryDTO(
                        repo.getName(),
                        repo.getOwner().getLogin(),
                        repo.getBranches().stream()
                                .map(branch -> new BranchDTO(branch.getName(), branch.getCommit().getSHA()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseList);
    }

    private Mono<Throwable> handleGithubApiError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new GithubApiException(
                        response.statusCode(),
                        extractMessageFromGithubError(body)
                )));
    }

    private String extractMessageFromGithubError(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            return root.has("message") ? root.get("message").asText() : "Unknown error";
        } catch (Exception e) {
            return "Invalid error response";
        }
    }
}
