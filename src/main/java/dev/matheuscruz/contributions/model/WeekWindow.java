package dev.matheuscruz.contributions.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public record WeekWindow(LocalDate since, LocalDate until, String label) {

    public static WeekWindow currentWorkWeek(LocalDate today) {
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate friday = monday.plusDays(4);
        return new WeekWindow(monday, friday, formatLabel(monday, friday));
    }

    public static WeekWindow of(LocalDate since, LocalDate until) {
        return new WeekWindow(since, until, formatLabel(since, until));
    }

    private static String formatLabel(LocalDate since, LocalDate until) {
        if (since.getMonth() == until.getMonth() && since.getYear() == until.getYear()) {
            DateTimeFormatter day = DateTimeFormatter.ofPattern("d");
            DateTimeFormatter monthYear = DateTimeFormatter.ofPattern("MMMM yyyy");
            return "%s-%s %s".formatted(day.format(since), day.format(until), monthYear.format(until));
        }
        DateTimeFormatter full = DateTimeFormatter.ofPattern("d MMMM");
        return "%s - %s %d".formatted(full.format(since), full.format(until), until.getYear());
    }
}
