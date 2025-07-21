package com.example.rekrutacja_atipera.model;

import org.springframework.http.HttpStatusCode;

public class GithubApiException extends RuntimeException {
    private final HttpStatusCode status;
    private final String body;

    public GithubApiException(HttpStatusCode status, String body) {
        super("GitHub API error: " + status + " - " + body);
        this.status = status;
        this.body = body;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }
}
