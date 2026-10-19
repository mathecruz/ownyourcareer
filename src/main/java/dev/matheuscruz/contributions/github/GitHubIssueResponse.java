package dev.matheuscruz.contributions.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubIssueResponse(int number, String html_url) {
}
