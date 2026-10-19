package dev.matheuscruz.contributions.github;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubSearchResponse(List<Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(int number, String title, String body, String html_url, String repository_url,
            String state, boolean draft, PullRequestInfo pull_request) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PullRequestInfo(String merged_at) {
    }
}
