package org.astronatalai.backend.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.astronatalai.backend.entity.BirthChart;
import tools.jackson.databind.JsonNode;

public record ChartResponse(
        UUID id,
        String nombre,
        LocalDate fechaNac,
        LocalTime horaNac,
        boolean timeKnown,
        String ciudad,
        double latitud,
        double longitud,
        String timezone,
        JsonNode datosCarta,
        Instant creadoEn) {

    public static ChartResponse from(BirthChart chart) {
        return new ChartResponse(
                chart.getId(),
                chart.getNombre(),
                chart.getFechaNac(),
                chart.getHoraNac(),
                chart.isTimeKnown(),
                chart.getCiudad(),
                chart.getLatitud(),
                chart.getLongitud(),
                chart.getTimezone(),
                chart.getDatosCarta(),
                chart.getCreadoEn());
    }
}
