package dev.matheuscruz.contributions;

public record ExecutionInput(String since, String until) {

    public boolean hasCustomRange() {
        return since != null && until != null;
    }
}
