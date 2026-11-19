package org.astronatalai.backend.service;

import java.time.Clock;
import java.time.LocalDate;
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
import org.astronatalai.backend.repository.BirthChartRepository;
import org.astronatalai.backend.repository.ReadingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
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

    public ReadingResponse generate(UUID userId, CreateReadingRequest request) {
        ReadingType type = ReadingType.from(request.type());
        BirthChart chart = birthChartRepository.findByIdAndUser_Id(request.chartId(), userId)
                .orElseThrow(ChartNotFoundException::new);

        LocalDate today = LocalDate.now(clock);
        LocalDate rangeDate = type == ReadingType.NATAL ? chart.getFechaNac() : today;
        ReadingType.Range range = type.range(rangeDate);

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
}
