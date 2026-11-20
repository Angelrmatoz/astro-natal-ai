package org.astronatalai.backend.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.astronatalai.backend.entity.Reading;

public record ReadingResponse(
        UUID id,
        UUID chartId,
        String tipo,
        LocalDate rangoInicio,
        LocalDate rangoFin,
        String contenidoTexto,
        String modelo,
        Instant creadoEn) {

    public static ReadingResponse from(Reading reading) {
        return new ReadingResponse(
                reading.getId(),
                reading.getChart().getId(),
                reading.getTipo(),
                reading.getRangoInicio(),
                reading.getRangoFin(),
                reading.getContenidoTexto(),
                reading.getModelo(),
                reading.getCreadoEn());
    }
}
