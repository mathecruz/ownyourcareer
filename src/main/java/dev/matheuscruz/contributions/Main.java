package dev.matheuscruz.contributions;

import java.util.Map;

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
        String result = workflow.startInstance(Map.of())
                .await().indefinitely()
                .as(String.class)
                .orElse("no contributions generated");
        System.out.println(result);
        return 0;
    }
}
