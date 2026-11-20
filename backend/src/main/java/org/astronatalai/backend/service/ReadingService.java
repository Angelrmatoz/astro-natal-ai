package org.astronatalai.backend.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.client.AstroEngineClient;
import org.astronatalai.backend.client.GemmaClient;
import org.astronatalai.backend.dto.CreateReadingRequest;
import org.astronatalai.backend.dto.ReadingResponse;
import org.astronatalai.backend.entity.BirthChart;
import org.astronatalai.backend.entity.Reading;
import org.astronatalai.backend.exception.ChartNotFoundException;
import org.astronatalai.backend.exception.ReadingNotFoundException;
import org.astronatalai.backend.repository.BirthChartRepository;
import org.astronatalai.backend.repository.ReadingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final BirthChartRepository birthChartRepository;
    private final ReadingRepository readingRepository;
    private final AstroEngineClient astroEngineClient;
    private final GemmaClient gemmaClient;
    private final PromptBuilder promptBuilder;
    private final Clock clock;

    @Transactional
    public ReadingResponse generate(UUID userId, CreateReadingRequest request) {
        ReadingType type = ReadingType.from(request.type());
        BirthChart chart = birthChartRepository.findByIdAndUser_Id(request.chartId(), userId)
                .orElseThrow(ChartNotFoundException::new);

        LocalDate today = localToday(chart);
        ReadingType.Range range = rangeFor(chart, type);

        Optional<Reading> existing = readingRepository
                .findByChartIdAndTipoAndRangoInicio(chart.getId(), type.value(), range.start());
        if (existing.isPresent()) {
            return ReadingResponse.from(existing.get());
        }

        JsonNode transits = type == ReadingType.NATAL
                ? null
                : astroEngineClient.transits(chart.getDatosCarta(), today);
        String prompt = promptBuilder.build(chart, transits, type, range);
        GemmaClient.GeneratedText generated = gemmaClient.generate(prompt);

        JsonNode snapshot = transits == null ? chart.getDatosCarta() : transits;
        Reading reading = new Reading(
                chart,
                type.value(),
                range.start(),
                range.end(),
                snapshot,
                generated.text(),
                generated.model());

        try {
            return ReadingResponse.from(readingRepository.saveAndFlush(reading));
        } catch (DataIntegrityViolationException exception) {
            return readingRepository
                    .findByChartIdAndTipoAndRangoInicio(chart.getId(), type.value(), range.start())
                    .map(ReadingResponse::from)
                    .orElseThrow(() -> exception);
        }
    }

    /**
     * Devuelve la lectura vigente del periodo sin generar nada. El frontend la usa para
     * mostrar "ya tienes tu lectura" en vez de volver a devolver el mismo texto.
     */
    @Transactional(readOnly = true)
    public ReadingResponse current(UUID userId, UUID chartId, String typeValue) {
        ReadingType type = ReadingType.from(typeValue);
        BirthChart chart = birthChartRepository.findByIdAndUser_Id(chartId, userId)
                .orElseThrow(ChartNotFoundException::new);
        ReadingType.Range range = rangeFor(chart, type);
        return readingRepository
                .findByChartIdAndTipoAndRangoInicio(chart.getId(), type.value(), range.start())
                .map(ReadingResponse::from)
                .orElseThrow(ReadingNotFoundException::new);
    }

    /**
     * El "hoy" se calcula en la zona horaria de la carta: la lectura diaria cambia a
     * medianoche local, la semanal el lunes y la mensual el día 1.
     */
    private LocalDate localToday(BirthChart chart) {
        String timezone = chart.getUser() != null ? chart.getUser().getTimezone() : null;
        if (timezone == null || timezone.isBlank()) {
            timezone = chart.getTimezone();
        }
        if (timezone == null || timezone.isBlank()) {
            return LocalDate.now(clock);
        }
        try {
            return LocalDate.now(clock.withZone(ZoneId.of(timezone)));
        } catch (java.time.DateTimeException exception) {
            return LocalDate.now(clock);
        }
    }

    private ReadingType.Range rangeFor(BirthChart chart, ReadingType type) {
        LocalDate today = localToday(chart);
        LocalDate rangeDate = type == ReadingType.NATAL ? chart.getFechaNac() : today;
        return type.range(rangeDate);
    }
}
