package com.example.rekrutacja_atipera.controller;

import com.example.rekrutacja_atipera.model.Branch;
import com.example.rekrutacja_atipera.model.dto.BranchDTO;
import com.example.rekrutacja_atipera.model.dto.RepositoryDTO;
import com.example.rekrutacja_atipera.exception.GithubApiException;
import com.example.rekrutacja_atipera.model.Repository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Value("${github.token}")
    private String githubToken;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public UserController(ObjectMapper objectMapper, WebClient webClient) {
        this.webClient = webClient;
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
                .map(filteredRepos -> {
                    List<RepositoryDTO> responseList = filteredRepos.stream()
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
