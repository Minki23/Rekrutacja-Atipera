package com.example.rekrutacja_atipera.model;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    private String name;
    private boolean fork;
    private List<Branch> branches = new ArrayList<>();

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public boolean isFork() {
        return fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
