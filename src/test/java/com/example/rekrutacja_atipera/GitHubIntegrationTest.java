package com.example.rekrutacja_atipera;

import com.example.rekrutacja_atipera.model.dto.RepositoryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = RekrutacjaAtiperaApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class GitHubIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnRepositoriesWithBranchesForValidUser() {
        String username = "octocat";

        ResponseEntity<List<RepositoryDTO>> response = restTemplate.exchange(
                "/?username={user}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {},
                username
        );

        List<RepositoryDTO> repos = response.getBody();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(repos).isNotEmpty();
        assertThat(repos)
                .allSatisfy(repo -> {
                    assertThat(repo.getOwnerLogin()).isEqualTo(username);
                    assertThat(repo.getBranches()).isNotEmpty();
                    repo.getBranches().forEach(branch -> {
                        assertThat(branch.getName()).isNotNull();
                        assertThat(branch.getLastCommitSha()).isNotEmpty();
                    });
                });
    }
}
