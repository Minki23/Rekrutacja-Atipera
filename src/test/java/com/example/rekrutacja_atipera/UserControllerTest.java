package com.example.rekrutacja_atipera;

import com.example.rekrutacja_atipera.controller.UserController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WebFluxTest(UserController.class)
@Import(UserControllerTest.TestConfig.class)
@TestPropertySource(properties = {"github.token=test-token"})
class UserControllerTest {

        @Autowired
        private WebTestClient webTestClient;

        private WireMockServer wireMockServer;

        @TestConfiguration
        static class TestConfig {
                @Bean
                public WebClient webClient() {
                        return WebClient.builder()
                                        .baseUrl("http://localhost:8089")
                                        .build();
                }

                @Bean
                public ObjectMapper objectMapper() {
                        return new ObjectMapper();
                }
        }

        @BeforeEach
        void setUp() {
                wireMockServer = new WireMockServer(8089);
                wireMockServer.start();
                WireMock.configureFor("localhost", 8089);
        }

        @AfterEach
        void tearDown() {
                wireMockServer.stop();
        }

        @Test
        void shouldReturnRepositoriesWithBranchesForValidUser() {
                String username = "testuser";

                stubFor(get(urlEqualTo("/users/testuser/repos"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("""
                                                                [
                                                                        {
                                                                                "name": "my-project",
                                                                                "fork": false,
                                                                                "owner": {
                                                                                        "login": "testuser"
                                                                                }
                                                                        },
                                                                        {
                                                                                "name": "forked-repo",
                                                                                "fork": true,
                                                                                "owner": {
                                                                                        "login": "testuser"
                                                                                }
                                                                        },
                                                                        {
                                                                                "name": "another-project",
                                                                                "fork": false,
                                                                                "owner": {
                                                                                        "login": "testuser"
                                                                                }
                                                                        }
                                                                ]
                                                                """)));

                stubFor(get(urlEqualTo("/repos/testuser/my-project/branches"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("""
                                                                [
                                                                        {
                                                                                "name": "main",
                                                                                "commit": {
                                                                                        "sha": "abc123def456"
                                                                                }
                                                                        },
                                                                        {
                                                                                "name": "feature-branch",
                                                                                "commit": {
                                                                                        "sha": "def456ghi789"
                                                                                }
                                                                        }
                                                                ]
                                                                """)));

                stubFor(get(urlEqualTo("/repos/testuser/another-project/branches"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("""
                                                                [
                                                                        {
                                                                                "name": "master",
                                                                                "commit": {
                                                                                        "sha": "ghi789jkl012"
                                                                                }
                                                                        }
                                                                ]
                                                                """)));

                webTestClient.get()
                                .uri("/?username={username}", username)
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isOk()
                                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                                .expectBody()
                                .jsonPath("$.length()").isEqualTo(2)
                                .jsonPath("$[0].repositoryName").isEqualTo("my-project")
                                .jsonPath("$[0].ownerLogin").isEqualTo("testuser")
                                .jsonPath("$[0].branches.length()").isEqualTo(2)
                                .jsonPath("$[0].branches[0].name").isEqualTo("main")
                                .jsonPath("$[0].branches[0].lastCommitSha").isEqualTo("abc123def456")
                                .jsonPath("$[0].branches[1].name").isEqualTo("feature-branch")
                                .jsonPath("$[0].branches[1].lastCommitSha").isEqualTo("def456ghi789")
                                .jsonPath("$[1].repositoryName").isEqualTo("another-project")
                                .jsonPath("$[1].ownerLogin").isEqualTo("testuser")
                                .jsonPath("$[1].branches.length()").isEqualTo(1)
                                .jsonPath("$[1].branches[0].name").isEqualTo("master")
                                .jsonPath("$[1].branches[0].lastCommitSha").isEqualTo("ghi789jkl012");

                verify(1, getRequestedFor(urlEqualTo("/users/testuser/repos")));
                verify(1, getRequestedFor(urlEqualTo("/repos/testuser/my-project/branches")));
                verify(1, getRequestedFor(urlEqualTo("/repos/testuser/another-project/branches")));
                verify(0, getRequestedFor(urlEqualTo("/repos/testuser/forked-repo/branches")));
        }
}