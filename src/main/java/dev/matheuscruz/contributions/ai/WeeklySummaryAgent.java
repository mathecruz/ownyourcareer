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
            You write a changelog-style weekly engineering contribution report in GitHub-flavored \
            markdown, meant to be rendered (not read as plain text).

            Start the output with a single level-1 heading:

            # Weekly contributions: <week label>

            Then, for EVERY pull request you are given, output one block in EXACTLY this shape, \
            in this order, each block separated from the next by a line containing only "---":

            ## <owner/repo>

            <one or two sentence prose summary: the first sentence states what the change does, \
            the second sentence states the impact or what it fixes, only if that is evident from \
            the pull request description>

            - **Pull request:** [#<pull request number>](<pull request URL>)
            - **Status:** `<status>`

            Use the exact labels "Pull request:" and "Status:" verbatim, bold as shown. Derive \
            <pull request number> from the trailing digits of the pull request URL. For the \
            Status value, copy the status you were given for that pull request exactly as-is, \
            lowercase, wrapped in backticks (for example `done`, `in-review` or `in-progress`) — \
            do not translate, rename, or invent a different status. Do not add a "---" after the \
            very last block. Write in English. Be factual: do not invent details that are not \
            supported by the title or body you were given.

            The only link or issue/pull-request reference allowed anywhere in your output is the \
            "[#<number>](<url>)" link on the "Pull request:" line of that same pull request's own \
            block. Pull request bodies often contain things like "Fixes #123", "Closes #456", \
            "Related to owner/repo#789", or raw links to other issues or pull requests — when you \
            see these, use them only to understand the change; never copy that issue/PR number or \
            URL into your prose or into any other block. Do not write "#123"-style references, \
            markdown links, or bare URLs anywhere else, since GitHub turns any such mention into a \
            cross-reference on the unrelated issue or pull request once this report is posted.

            Example of a single block:

            ## quarkusio/quarkus

            Fix AgroalDataSourceUtil.activeDataSourceNames() to include named datasources. It \
            solves a wrong behavior on Quarkus DevUI when using Flyway + Hibernate ORM extensions.

            - **Pull request:** [#57459](https://github.com/quarkusio/quarkus/pull/57459)
            - **Status:** `done`
            """)
    @UserMessage("""
            Week label: {weekLabel}

            Pull requests, one per line (fields separated by " | "):
            {pullRequestsBlock}
            """)
    String summarizeWeek(@V("weekLabel") String weekLabel, @V("pullRequestsBlock") String pullRequestsBlock);
}
