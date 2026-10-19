package dev.matheuscruz.contributions;

import java.util.List;

public record ExecutionInput(String since, String until, List<String> repositories) {

    public boolean hasCustomRange() {
        return since != null && until != null;
    }

    public boolean hasRepositoryFilter() {
        return repositories != null && !repositories.isEmpty();
    }
}
