package org.astronatalai.backend.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import org.astronatalai.backend.exception.InvalidReadingRequestException;

public enum ReadingType {
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String value;

    ReadingType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ReadingType from(String value) {
        return switch (value == null ? "" : value.toLowerCase(Locale.ROOT)) {
            case "day" -> DAY;
            case "week" -> WEEK;
            case "month" -> MONTH;
            default -> throw new InvalidReadingRequestException(
                    "type debe ser day, week o month");
        };
    }

    public Range range(LocalDate date) {
        return switch (this) {
            case DAY -> new Range(date, date);
            case WEEK -> {
                LocalDate start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new Range(start, start.plusDays(6));
            }
            case MONTH -> new Range(
                    date.withDayOfMonth(1),
                    date.with(TemporalAdjusters.lastDayOfMonth()));
        };
    }

    public record Range(LocalDate start, LocalDate end) {
    }
}
