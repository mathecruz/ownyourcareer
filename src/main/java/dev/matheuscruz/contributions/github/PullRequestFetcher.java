package dev.matheuscruz.contributions.github;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import dev.matheuscruz.contributions.model.GitHubPullRequest;
import dev.matheuscruz.contributions.model.WeekWindow;

@ApplicationScoped
public class PullRequestFetcher {

    @Inject
    @RestClient
    GitHubSearchClient client;

    @ConfigProperty(name = "github.username")
    String username;

    @ConfigProperty(name = "github.token")
    String token;

    public List<GitHubPullRequest> fetchPullRequests(WeekWindow window) {
        Map<String, GitHubPullRequest> byKey = new LinkedHashMap<>();
        collect(byKey, window, "created");
        collect(byKey, window, "merged");
        return List.copyOf(byKey.values());
    }

    private void collect(Map<String, GitHubPullRequest> byKey, WeekWindow window, String dateField) {
        String query = "author:%s is:pr %s:%s..%s".formatted(username, dateField, window.since(), window.until());
        GitHubSearchResponse response = client.searchIssues(
                "Bearer " + token, "application/vnd.github+json", query, 100);
        for (GitHubSearchResponse.Item item : response.items()) {
            String project = toProject(item.repository_url());
            String key = project + "#" + item.number();
            byKey.putIfAbsent(key, new GitHubPullRequest(project, item.title(), item.body(), item.html_url()));
        }
    }

    private static String toProject(String repositoryUrl) {
        String[] parts = repositoryUrl.split("/repos/");
        return parts.length == 2 ? parts[1] : repositoryUrl;
    }
}
