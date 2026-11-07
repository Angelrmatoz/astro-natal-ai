package org.astronatalai.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record NatalChartCommand(
        LocalDate fecha,
        LocalTime hora,
        boolean timeKnown,
        double lat,
        double lng,
        String timezone) {
}
