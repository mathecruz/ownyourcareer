package dev.matheuscruz.contributions.model;

import java.time.OffsetDateTime;

public record GitHubPullRequest(
        String project,
        int number,
        String title,
        String body,
        String htmlUrl,
        OffsetDateTime createdAt,
        OffsetDateTime mergedAt) {

    public boolean merged() {
        return mergedAt != null;
    }
}
