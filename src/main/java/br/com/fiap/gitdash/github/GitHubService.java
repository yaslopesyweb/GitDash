package br.com.fiap.gitdash.github;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubService {

    private final RestTemplate restTemplate;

    public GitHubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<RepositoryInfo> getUserRepositories(String tokenValue) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenValue);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.github.com/user/repos?sort=created&direction=desc\";",
                HttpMethod.GET,
                entity,
                String.class
        );

        List<RepositoryInfo> repoInfos = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            for (JsonNode repo : root) {
                RepositoryInfo repoInfo = new RepositoryInfo();
                repoInfo.setName(repo.path("name").asText());
                repoInfo.setDescription(repo.path("description").asText());

                String commitsUrl = repo.path("commits_url").asText().replace("{/sha}", "");
                ResponseEntity<String> commitsResponse = restTemplate.exchange(
                        commitsUrl,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

                JsonNode commitsRoot = mapper.readTree(commitsResponse.getBody());
                if (commitsRoot.isArray() && !commitsRoot.isEmpty()) {
                    String lastCommitMessage = commitsRoot.get(0).path("commit").path("message").asText();
                    repoInfo.setLastCommitMessage(lastCommitMessage);
                    repoInfo.setCommitCount(commitsRoot.size());
                }

                repoInfos.add(repoInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return repoInfos;
    }
}