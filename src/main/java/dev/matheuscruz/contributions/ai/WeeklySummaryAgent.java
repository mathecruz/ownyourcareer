package dev.matheuscruz.contributions.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface WeeklySummaryAgent {

    @SystemMessage("""
            You write a changelog-style weekly engineering contribution report in markdown.

            For EVERY pull request you are given, output one block in EXACTLY this shape, \
            in this order, separated by a single blank line from the next block:

            Week: <week label, same value for every block>

            <one or two sentence prose summary: the first sentence states what the change does, \
            the second sentence states the impact or what it fixes, only if that is evident from \
            the pull request description>

            Pull request: <pull request URL>

            Project: <owner/repo>

            Status: [<status>]

            Use the exact labels "Week:", "Pull request:", "Project:" and "Status:" verbatim. \
            For the Status line, copy the status you were given for that pull request exactly \
            as-is, lowercase, wrapped in square brackets (for example "[done]", "[in-review]" \
            or "[in-progress]") — do not translate, rename, or invent a different status. Do \
            not add headings, bullet points, numbering, or wrap the output in a code block. \
            Write in English. Be factual: do not invent details that are not supported by the \
            title or body you were given.

            Example of a single block:

            Week: 19-23 October 2026

            Fix AgroalDataSourceUtil.activeDataSourceNames() to include named datasources. It \
            solves a wrong behavior on Quarkus DevUI when using Flyway + Hibernate ORM extensions.

            Pull request: https://github.com/quarkusio/quarkus/pull/57459

            Project: quarkusio/quarkus

            Status: [done]
            """)
    @UserMessage("""
            Week label: {weekLabel}

            Pull requests, one per line (fields separated by " | "):
            {pullRequestsBlock}
            """)
    String summarizeWeek(@V("weekLabel") String weekLabel, @V("pullRequestsBlock") String pullRequestsBlock);
}
