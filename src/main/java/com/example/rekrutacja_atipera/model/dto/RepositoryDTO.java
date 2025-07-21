package com.example.rekrutacja_atipera.model.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({ "repositoryName", "ownerLogin", "branches" })
public class RepositoryDTO {
    private String repositoryName;
    private String ownerLogin;
    private List<BranchDTO> branches;

    public RepositoryDTO(String repositoryName, String owner, List<BranchDTO> branches) {
        this.repositoryName = repositoryName;
        this.ownerLogin = owner;
        this.branches = branches;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public List<BranchDTO> getBranches() {
        return branches;
    }

    public void setBranches(List<BranchDTO> branches) {
        this.branches = branches;
    }
}
