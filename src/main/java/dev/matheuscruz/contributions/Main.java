package dev.matheuscruz.contributions;

import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
@Singleton
public class Main implements QuarkusApplication {

    @Inject
    WeeklyContributionsWorkflow workflow;

    public static void main(String... args) {
        Quarkus.run(Main.class, args);
    }

    @Override
    public int run(String... args) {
        ExecutionInput input = parseArgs(args);
        String result = workflow.startInstance(input)
                .await().indefinitely()
                .as(String.class)
                .orElse("no contributions generated");
        System.out.println(result);
        return 0;
    }

    private static ExecutionInput parseArgs(String... args) {
        String since = null;
        String until = null;
        List<String> repositories = List.of();
        for (String arg : args) {
            if (arg.startsWith("--since=")) {
                since = arg.substring("--since=".length());
            } else if (arg.startsWith("--until=")) {
                until = arg.substring("--until=".length());
            } else if (arg.startsWith("--repos=")) {
                repositories = Arrays.stream(arg.substring("--repos=".length()).split(","))
                        .map(String::strip)
                        .filter(repo -> !repo.isBlank())
                        .toList();
            }
        }
        return new ExecutionInput(since, until, repositories);
    }
}
