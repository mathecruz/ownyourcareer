# ownyourcareer

[![Docker Repository on Quay](https://quay.io/repository/mcruzdev/ownyourcareer/status "Docker Repository on Quay")](https://quay.io/repository/mcruzdev/ownyourcareer)

Opens a weekly GitHub issue summarizing your merged/opened pull requests, written by an LLM.
Built with Quarkus, [Quarkus Flow](https://docs.quarkiverse.io/quarkus-flow/dev/index.html)
(fetch PRs → summarize with langchain4j → open issue) and [Quarkus LangChain4j](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html) (OpenAI).

It runs as a **command-mode app**: it executes once, prints the created issue's URL, and exits —
no HTTP server stays up. `.github/workflows/weekly-contributions.yml` runs it every Friday via
`workflow_dispatch`/cron.

By default, it covers the current Monday–Friday. To report on a different range, pass
`--since=YYYY-MM-DD --until=YYYY-MM-DD`:

```shell script
java -jar target/quarkus-app/quarkus-run.jar --since=2026-10-12 --until=2026-10-16
```

By default, it searches pull requests across every repository the configured user has access to.
To restrict the search to specific repositories, pass a comma-separated `owner/repo` list via
`--repos`:

```shell script
java -jar target/quarkus-app/quarkus-run.jar --repos=quarkiverse/quarkus-flow,open-workflow-specification/sdk-java
```

The same overrides are exposed as `since`/`until`/`repos` inputs when running `weekly-contributions.yml`
manually via `workflow_dispatch`.

### Configuration

| Variable | Where | Purpose |
|---|---|---|
| `CONTRIBUTIONS_USERNAME` (repo **secret**) | Settings → Secrets and variables → Actions → Secrets | GitHub username whose PRs are collected |
| `CONTRIBUTIONS_GH_TOKEN` (repo **secret**) | Settings → Secrets and variables → Actions → Secrets | Classic PAT with `repo` scope — used both to search PRs across repos and to open the issue |
| `OPENAI_API_KEY` (repo **secret**) | same as above | Used by `quarkus-langchain4j-openai` |
| `contributions.issue.repository` (`CONTRIBUTIONS_ISSUE_REPOSITORY` env var, config only) | `application.properties` | `owner/repo` where the weekly issue is opened, defaults to `mathecruz/ownyourcareer` |

`.github/workflows/docker-publish.yml` builds the JVM image and pushes it to
[quay.io/mcruzdev/ownyourcareer](https://quay.io/repository/mcruzdev/ownyourcareer) on every push
to `main` (and on manual dispatch). It needs two extra repo **secrets**:

| Secret | Purpose |
|---|---|
| `QUAY_USERNAME` | Quay robot account name, e.g. `mcruzdev+ownyourcareer_ci` |
| `QUAY_PASSWORD` | That robot account's token |

Create a robot account under the repository's **Settings → Robot Accounts** on quay.io and grant
it **Write** access, rather than using your personal quay.io password.

### Releasing

Bump the version in the `VERSION` file (plain text, e.g. `0.2.0`) on a pull request. Once that
pull request is **merged** into `main` (or the workflow is run manually via `workflow_dispatch`),
`.github/workflows/release.yml` kicks in and, only if that tag doesn't already exist:

1. Creates and pushes the git tag `v<version>`.
2. Builds the jar and pushes `quay.io/mcruzdev/ownyourcareer:<version>` (uses the same
   `QUAY_USERNAME`/`QUAY_PASSWORD` secrets as above).

It does not touch the `:main` image that `docker-publish.yml` keeps publishing on every push to
`main` — the two workflows are independent.

Locally, export `GITHUB_USERNAME`, `GITHUB_TOKEN` and `OPENAI_API_KEY`, then:

```shell script
./mvnw package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/ownyourcareer-0.1.0-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.
