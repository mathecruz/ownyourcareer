package dev.matheuscruz.contributions.github;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class GitHubIssueReporter {

    private static final List<String> LABELS = List.of("generated-report");

    @Inject
    @RestClient
    GitHubIssuesClient client;

    @ConfigProperty(name = "github.token")
    String token;

    @ConfigProperty(name = "contributions.issue.repository")
    String repository;

    public String createIssue(String weekLabel, String body) {
        String[] ownerAndRepo = repository.split("/", 2);
        String title = "Weekly contributions: %s".formatted(weekLabel);
        GitHubIssueResponse response = client.createIssue(
                "Bearer " + token,
                "application/vnd.github+json",
                ownerAndRepo[0],
                ownerAndRepo[1],
                new GitHubIssueRequest(title, body, LABELS));
        return response.html_url();
    }
}
