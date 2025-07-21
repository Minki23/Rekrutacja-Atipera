package com.example.rekrutacja_atipera.model;

public class Repository {
    private String name;
    private boolean fork;

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
