package com.example.rekrutacja_atipera.model.dto;

public class BranchDTO {
    private final String name;
    private final String lastCommitSha;

    public BranchDTO(String name, String lastCommitSha) {
        this.name = name;
        this.lastCommitSha = lastCommitSha;
    }

    public String getName() {
        return name;
    }

    public String getLastCommitSha() {
        return lastCommitSha;
    }
}
