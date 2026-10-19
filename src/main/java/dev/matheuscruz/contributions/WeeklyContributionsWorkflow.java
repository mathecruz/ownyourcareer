package dev.matheuscruz.contributions;

import static io.quarkiverse.flow.dsl.FlowDSL.function;
import static io.quarkiverse.flow.dsl.FlowWorkflowBuilder.workflow;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.flow.Flow;
import io.serverlessworkflow.api.types.Workflow;

import dev.matheuscruz.contributions.ai.WeeklySummaryAgent;
import dev.matheuscruz.contributions.github.GitHubIssueReporter;
import dev.matheuscruz.contributions.github.PullRequestFetcher;
import dev.matheuscruz.contributions.model.GitHubPullRequest;
import dev.matheuscruz.contributions.model.WeekWindow;

@ApplicationScoped
public class WeeklyContributionsWorkflow extends Flow {

    @Inject
    PullRequestFetcher fetcher;

    @Inject
    WeeklySummaryAgent summaryAgent;

    @Inject
    GitHubIssueReporter issueReporter;

    @Override
    public Workflow descriptor() {
        return workflow("weekly-contributions")
                .tasks(
                        function("fetchPullRequests", this::fetchWeek,
                                ExecutionInput.class, WeekSummaryInput.class),
                        function("summarize",
                                (WeekSummaryInput input) -> new IssueDraft(input.weekLabel(),
                                        summaryAgent.summarizeWeek(input.weekLabel(), input.pullRequestsBlock())),
                                WeekSummaryInput.class, IssueDraft.class),
                        function("createIssue",
                                (IssueDraft draft) -> issueReporter.createIssue(draft.weekLabel(), draft.body()),
                                IssueDraft.class, String.class))
                .build();
    }

    private WeekSummaryInput fetchWeek(ExecutionInput input) {
        WeekWindow window = input != null && input.hasCustomRange()
                ? WeekWindow.of(LocalDate.parse(input.since()), LocalDate.parse(input.until()))
                : WeekWindow.currentWorkWeek(LocalDate.now());
        List<GitHubPullRequest> pullRequests = fetcher.fetchPullRequests(window);
        String block = pullRequests.stream()
                .map(pr -> "Title: %s | Body: %s | URL: %s | Project: %s".formatted(
                        pr.title(), safeBody(pr.body()), pr.htmlUrl(), pr.project()))
                .collect(Collectors.joining("\n"));
        return new WeekSummaryInput(window.label(), block.isBlank() ? "(no pull requests this week)" : block);
    }

    private static String safeBody(String body) {
        if (body == null || body.isBlank()) {
            return "(no description)";
        }
        return body.replace("\n", " ").replace("|", "/").strip();
    }
}
