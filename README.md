# GitHub Repository API

A Spring Boot REST API that fetches GitHub user repositories and their branches using the GitHub API.

## Overview

This application provides an endpoint to retrieve all non-fork repositories for a given GitHub user, including detailed branch information with commit SHAs.

## Features

- Fetch user repositories from GitHub API
- Filter out forked repositories
- Retrieve branch information for each repository
- Comprehensive error handling
- JSON response format

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.3**

## API Endpoints

### Get User Repositories

```http
GET /?username={username}
```

**Parameters:**
- `username` (required): GitHub username

**Response Format:**
```json
[
  {
    "repositoryName": "Hello-World",
    "ownerLogin": "octocat",
    "branches": [
      {
        "name": "master",
        "lastCommitSha": "6dcb09b5b57875f334f61aebed695e2e4193db5e"
      }
    ]
  }
]
```

**Error Response:**
```json
{
  "status": 404,
  "message": "Not Found"
}
```

### Test Coverage

The application includes:
- **Integration tests** ([`GitHubIntegrationTest`](src/test/java/com/example/rekrutacja_atipera/GitHubIntegrationTest.java)): Tests the complete API flow with real GitHub API calls
  
## Configuration

### Application Properties

```properties
spring.application.name=Rekrutacja_Atipera
github.token=${GITHUB_TOKEN}
```

### Environment Variables

- `GITHUB_TOKEN`: Your GitHub personal access token (optional but recommended for higher rate limits)

## GitHub API Rate Limits

- **Without authentication**: 60 requests per hour
- **With authentication**: 5,000 requests per hour
