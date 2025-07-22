package com.example.rekrutacja_atipera.controller;

import com.example.rekrutacja_atipera.model.dto.RepositoryDTO;
import com.example.rekrutacja_atipera.service.GitHubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private final GitHubService gitHubService;

    public UserController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getUserRepositories(@RequestParam String username) {
        try {
            List<RepositoryDTO> repositories = gitHubService.getUserRepositories(username);
            return ResponseEntity.ok(repositories);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "message", "Not Found"));
        }
    }
}