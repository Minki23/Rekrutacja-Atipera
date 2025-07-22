package com.example.rekrutacja_atipera.service;

import com.example.rekrutacja_atipera.model.dto.BranchDTO;
import com.example.rekrutacja_atipera.model.dto.RepositoryDTO;
import com.example.rekrutacja_atipera.model.Branch;
import com.example.rekrutacja_atipera.model.Repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubService implements IGitHubService {

    private final RestTemplate restTemplate;
    private final String githubToken;

    public GitHubService(RestTemplate restTemplate, @Value("${github.token:}") String githubToken) {
        this.restTemplate = restTemplate;
        this.githubToken = githubToken;
    }

    public List<RepositoryDTO> getUserRepositories(String username) {
        try {
            String url = "https://api.github.com/users/" + username + "/repos";
            Repository[] repositories = restTemplate.exchange(url, HttpMethod.GET, createEntity(), Repository[].class).getBody();

            return Arrays.stream(repositories)
                    .filter(repo -> !repo.isFork())
                    .map(repo -> new RepositoryDTO(
                            repo.getName(),
                            repo.getOwner().getLogin(),
                            getBranches(username, repo.getName())
                    ))
                    .collect(Collectors.toList());

        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("User not found", e);
        }
    }

    private List<BranchDTO> getBranches(String username, String repoName) {
        String url = "https://api.github.com/repos/" + username + "/" + repoName + "/branches";
        Branch[] branches = restTemplate.exchange(url, HttpMethod.GET, createEntity(), Branch[].class).getBody();

        return Arrays.stream(branches)
                .map(branch -> new BranchDTO(branch.getName(), branch.getCommit().getSHA()))
                .collect(Collectors.toList());
    }

    private HttpEntity<String> createEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3+json");
        if (!githubToken.isEmpty()) {
            headers.set("Authorization", "token " + githubToken);
        }
        return new HttpEntity<>(headers);
    }
}
