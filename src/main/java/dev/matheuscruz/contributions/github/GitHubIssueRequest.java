package dev.matheuscruz.contributions.github;

import java.util.List;

public record GitHubIssueRequest(String title, String body, List<String> labels) {
}
