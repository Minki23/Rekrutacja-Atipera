package com.example.rekrutacja_atipera.service;

import com.example.rekrutacja_atipera.model.dto.RepositoryDTO;

import java.util.List;

public interface IGitHubService {
    List<RepositoryDTO> getUserRepositories(String username);
}
