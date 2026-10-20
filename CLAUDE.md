# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Quarkus **command-mode** app (no HTTP server): it runs once, fetches the configured GitHub
user's pull requests for a date window, has an LLM (via Quarkus LangChain4j / OpenAI) write a
changelog-style weekly summary, prints that summary to stdout, and exits. Driven on a schedule by
`.github/workflows/weekly-contributions.yml` (Friday cron + manual `workflow_dispatch` with
`since`/`until`/`repos` overrides), which logs the report both to the step's plain CI log and to
the run's job summary via `$GITHUB_STEP_SUMMARY` — there is no GitHub issue created.

## Commands

```shell script
./mvnw quarkus:dev                 # live-coding dev mode (Dev UI at http://localhost:8080/q/dev/)
./mvnw package -DskipTests         # build target/quarkus-app/quarkus-run.jar (how CI builds it)
./mvnw package                     # build + run tests
./mvnw test -Dtest=ClassName       # run a single test class
./mvnw package -Dnative            # native executable (needs GraalVM, or add -Dquarkus.native.container-build=true)
```

Running it locally requires `GITHUB_USERNAME`, `GITHUB_TOKEN`, `OPENAI_API_KEY` exported (see
`src/main/resources/application.properties` for every config key and its env var):

```shell script
java -jar target/quarkus-app/quarkus-run.jar                                  # current Mon–Fri, all repos
java -jar target/quarkus-app/quarkus-run.jar --since=2026-10-12 --until=2026-10-16
java -jar target/quarkus-app/quarkus-run.jar --repos=quarkiverse/quarkus-flow,open-workflow-specification/sdk-java
```

There is no `src/test` yet — `./mvnw package` currently builds without running anything.

## Architecture

The entire pipeline is a **Quarkus Flow** (serverless-workflow DSL) with exactly two steps,
declared declaratively in `WeeklyContributionsWorkflow.descriptor()`:

```
fetchPullRequests (ExecutionInput -> WeekSummaryInput)
        -> summarize (WeekSummaryInput -> String markdown report)
```

`Main` (the `@QuarkusMain` entry point) parses `--since`/`--until`/`--repos` into an
`ExecutionInput`, starts the flow with `workflow.startInstance(input).await().indefinitely()`,
prints the resulting markdown report to stdout, and exits with code 0 — there's no
retry/error-recovery logic beyond what Quarkus Flow does.

Step-by-step:

1. **`fetchWeek`** resolves an `ExecutionInput` into a `WeekWindow` (current Mon–Fri via
   `WeekWindow.currentWorkWeek`, or a custom range via `WeekWindow.of` when both `since`/`until`
   are set — see `ExecutionInput.hasCustomRange()`), then calls `PullRequestFetcher`.
   `PullRequestFetcher` queries the GitHub search API **twice** per window — once filtering on
   `created:<range>` and once on `merged:<range>` — because a PR merged this week but created
   earlier would be missed by a single `created:` query. When `ExecutionInput.repositories()`
   (`--repos=owner/repo,...`) is non-empty, it instead runs that `created`/`merged` pair **once
   per repo**, appending a `repo:` qualifier to each query — GitHub's search API ANDs repeated
   qualifiers of the same type, so there's no way to OR multiple `repo:` values into a single
   query. With no `--repos`, it searches every repo the author has access to in one query pair.
   Results are deduped by `project#number` into a `LinkedHashMap` to preserve first-seen order.
   PR status (`done`/`closed`/`in-progress`/`in-review`) is derived in
   `PullRequestFetcher.toStatus` from `merged_at`/`state`/`draft`, not from any GitHub label. The
   PR list is flattened into one `" | "`-delimited line per PR (`WeekSummaryInput.pullRequestsBlock`)
   before being handed to the LLM.
2. **`summarize`** calls `WeeklySummaryAgent` (a `@RegisterAiService` langchain4j interface —
   there is no implementation class; the body is generated at build time from the `@SystemMessage`
   /`@UserMessage` prompt templates), returning the final markdown report string. The system
   prompt is strict about output shape: a single `# Weekly contributions: <week label>` heading,
   then one `## <owner/repo>`/prose/`- **Pull request:**`/`- **Status:**` block per PR separated
   by `---` rules, labels verbatim, status copied through unchanged and lowercase, and no issue/PR
   cross-references other than that PR's own `[#<number>](<url>)` link (so the report never
   creates GitHub back-links when logged). If you change the expected output format, the prompt in
   `WeeklySummaryAgent` is the single source of truth to edit.

`GitHubSearchClient` (`/search/issues`) is the only REST client, sharing the `github-api`
configKey/base URL (`quarkus.rest-client.github-api.url`). It takes the bearer token and `Accept`
header per-call rather than via a shared filter — add new GitHub endpoints the same way rather
than introducing a client-level auth filter unless multiple clients start needing it.

Records carry data between flow steps and model GitHub data: `ExecutionInput`, `WeekSummaryInput`
(flow step payloads), `GitHubPullRequest`, `WeekWindow` (domain), and the `GitHubSearchResponse`
REST DTO under `github/`.

## Configuration

All runtime config is in `src/main/resources/application.properties`, each key backed by an env
var with a default (e.g. `github.username=${GITHUB_USERNAME:mathecruz}`). Read that file before
adding a new external dependency or secret — it's the only place config keys, their env vars, and
defaults are declared. The README's configuration table documents which of these are GitHub repo
secrets vs. `vars` and why (e.g. `CONTRIBUTIONS_USERNAME` is a repo **variable**, not a secret).

## Releasing

Version bumps go in the plain-text `VERSION` file. A PR that changes `VERSION` and gets merged to
`main` triggers `.github/workflows/release.yml`, which tags `v<version>` (skipping if the tag
already exists) and pushes `quay.io/mcruzdev/ownyourcareer:<version>`. This is independent of
`docker-publish.yml`, which pushes the `:main` tag on every push to `main` regardless of `VERSION`.
