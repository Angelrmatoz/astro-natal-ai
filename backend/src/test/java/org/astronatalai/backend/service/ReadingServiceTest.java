package org.astronatalai.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.astronatalai.backend.client.AstroEngineClient;
import org.astronatalai.backend.client.GemmaClient;
import org.astronatalai.backend.dto.CreateReadingRequest;
import org.astronatalai.backend.dto.ReadingResponse;
import org.astronatalai.backend.entity.BirthChart;
import org.astronatalai.backend.entity.Reading;
import org.astronatalai.backend.entity.User;
import org.astronatalai.backend.exception.ChartNotFoundException;
import org.astronatalai.backend.repository.BirthChartRepository;
import org.astronatalai.backend.repository.ReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class ReadingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 14);
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CHART_ID = UUID.randomUUID();

    @Mock
    private BirthChartRepository birthChartRepository;

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private AstroEngineClient astroEngineClient;

    @Mock
    private GemmaClient gemmaClient;

    @Mock
    private PromptBuilder promptBuilder;

    private ReadingService readingService;
    private BirthChart chart;
    private JsonNode chartData;
    private JsonNode transitData;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC);
        readingService = new ReadingService(
                birthChartRepository, readingRepository, astroEngineClient,
                gemmaClient, promptBuilder, clock);

        chart = mock(BirthChart.class);
        chartData = JsonMapper.builder().build().readTree("""
                {"planets": [{"name": "Sun"}]}
                """);
        transitData = JsonMapper.builder().build().readTree("""
                {"aspects": [{"type": "square"}]}
                """);
    }

    @Test
    void generatesAndPersistsReading() {
        when(chart.getId()).thenReturn(CHART_ID);
        when(birthChartRepository.findByIdAndUser_Id(CHART_ID, USER_ID))
                .thenReturn(Optional.of(chart));
        when(readingRepository.findByChartIdAndTipoAndRangoInicio(
                CHART_ID, "day", TODAY)).thenReturn(Optional.empty());
        when(chart.getDatosCarta()).thenReturn(chartData);
        when(astroEngineClient.transits(chartData, TODAY)).thenReturn(transitData);
        when(promptBuilder.build(eq(chart), eq(transitData), eq(ReadingType.DAY), any()))
                .thenReturn("prompt");
        when(gemmaClient.generate("prompt"))
                .thenReturn(new GemmaClient.GeneratedText("lectura diaria", "gemma-4-31b-it"));
        when(readingRepository.saveAndFlush(any(Reading.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReadingResponse response = readingService.generate(
                USER_ID, new CreateReadingRequest(CHART_ID, "day"));

        assertThat(response.contenidoTexto()).isEqualTo("lectura diaria");
        assertThat(response.tipo()).isEqualTo("day");
        assertThat(response.rangoInicio()).isEqualTo(TODAY);
        assertThat(response.rangoFin()).isEqualTo(TODAY);

        ArgumentCaptor<Reading> saved = ArgumentCaptor.forClass(Reading.class);
        verify(readingRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getTransitosSnapshot()).isEqualTo(transitData);
        assertThat(saved.getValue().getModelo()).isEqualTo("gemma-4-31b-it");
    }

    @Test
    void generatesNatalReadingWithoutCallingEngine() {
        LocalDate birthDate = LocalDate.of(1990, 5, 1);
        when(chart.getId()).thenReturn(CHART_ID);
        when(chart.getFechaNac()).thenReturn(birthDate);
        when(chart.getDatosCarta()).thenReturn(chartData);
        when(birthChartRepository.findByIdAndUser_Id(CHART_ID, USER_ID))
                .thenReturn(Optional.of(chart));
        when(readingRepository.findByChartIdAndTipoAndRangoInicio(
                CHART_ID, "natal", birthDate)).thenReturn(Optional.empty());
        when(promptBuilder.build(eq(chart), isNull(), eq(ReadingType.NATAL), any()))
                .thenReturn("prompt natal");
        when(gemmaClient.generate("prompt natal"))
                .thenReturn(new GemmaClient.GeneratedText("carta interpretada", "gemma-4-31b-it"));
        when(readingRepository.saveAndFlush(any(Reading.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReadingResponse response = readingService.generate(
                USER_ID, new CreateReadingRequest(CHART_ID, "natal"));

        assertThat(response.tipo()).isEqualTo("natal");
        assertThat(response.rangoInicio()).isEqualTo(birthDate);
        assertThat(response.rangoFin()).isEqualTo(birthDate);
        verifyNoInteractions(astroEngineClient);

        ArgumentCaptor<Reading> saved = ArgumentCaptor.forClass(Reading.class);
        verify(readingRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getTransitosSnapshot()).isEqualTo(chartData);
    }

    @Test
    void dailyReadingUsesTheChartTimezoneForMidnight() {
        LocalDate localDay = LocalDate.of(2026, 11, 15);
        Clock localClock = Clock.fixed(
                Instant.parse("2026-11-16T03:00:00Z"), ZoneOffset.UTC);
        ReadingService service = new ReadingService(
                birthChartRepository, readingRepository, astroEngineClient,
                gemmaClient, promptBuilder, localClock);

        when(chart.getId()).thenReturn(CHART_ID);
        when(chart.getTimezone()).thenReturn("America/Santo_Domingo");
        when(chart.getDatosCarta()).thenReturn(chartData);
        when(birthChartRepository.findByIdAndUser_Id(CHART_ID, USER_ID))
                .thenReturn(Optional.of(chart));
        when(readingRepository.findByChartIdAndTipoAndRangoInicio(
                CHART_ID, "day", localDay)).thenReturn(Optional.empty());
        when(astroEngineClient.transits(chartData, localDay)).thenReturn(transitData);
        when(promptBuilder.build(eq(chart), eq(transitData), eq(ReadingType.DAY), any()))
                .thenReturn("prompt");
        when(gemmaClient.generate("prompt"))
                .thenReturn(new GemmaClient.GeneratedText("lectura local", "gemma-4-31b-it"));
        when(readingRepository.saveAndFlush(any(Reading.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReadingResponse response = service.generate(
                USER_ID, new CreateReadingRequest(CHART_ID, "day"));

        assertThat(response.rangoInicio()).isEqualTo(localDay);
        assertThat(response.rangoFin()).isEqualTo(localDay);
    }

    @Test
    void dailyReadingPrefersTheUserTimezone() {
        LocalDate madridDay = LocalDate.of(2026, 11, 16);
        Clock madridClock = Clock.fixed(
                Instant.parse("2026-11-15T23:30:00Z"), ZoneOffset.UTC);
        ReadingService service = new ReadingService(
                birthChartRepository, readingRepository, astroEngineClient,
                gemmaClient, promptBuilder, madridClock);

        User user = new User("ana@example.com", "hash", "Ana");
        user.updateTimezone("Europe/Madrid");
        when(chart.getId()).thenReturn(CHART_ID);
        when(chart.getUser()).thenReturn(user);
        when(chart.getDatosCarta()).thenReturn(chartData);
        when(birthChartRepository.findByIdAndUser_Id(CHART_ID, USER_ID))
                .thenReturn(Optional.of(chart));
        when(readingRepository.findByChartIdAndTipoAndRangoInicio(
                CHART_ID, "day", madridDay)).thenReturn(Optional.empty());
        when(astroEngineClient.transits(chartData, madridDay)).thenReturn(transitData);
        when(promptBuilder.build(eq(chart), eq(transitData), eq(ReadingType.DAY), any()))
                .thenReturn("prompt");
        when(gemmaClient.generate("prompt"))
                .thenReturn(new GemmaClient.GeneratedText("lectura madrid", "gemma-4-31b-it"));
        when(readingRepository.saveAndFlush(any(Reading.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReadingResponse response = service.generate(
                USER_ID, new CreateReadingRequest(CHART_ID, "day"));

        assertThat(response.rangoInicio()).isEqualTo(madridDay);
    }

    @Test
    void returnsStoredReadingWithoutCallingExternals() {
        when(chart.getId()).thenReturn(CHART_ID);
        Reading existing = new Reading(
                chart, "day", TODAY, TODAY, transitData, "lectura guardada", "gemma-4-31b-it");
        when(birthChartRepository.findByIdAndUser_Id(CHART_ID, USER_ID))
                .thenReturn(Optional.of(chart));
        when(readingRepository.findByChartIdAndTipoAndRangoInicio(
                CHART_ID, "day", TODAY)).thenReturn(Optional.of(existing));

        ReadingResponse response = readingService.generate(
                USER_ID, new CreateReadingRequest(CHART_ID, "day"));

        assertThat(response.id()).isEqualTo(existing.getId());
        assertThat(response.contenidoTexto()).isEqualTo("lectura guardada");
        verifyNoInteractions(astroEngineClient, gemmaClient, promptBuilder);
    }

    @Test
    void rejectsChartOwnedByAnotherUser() {
        when(birthChartRepository.findByIdAndUser_Id(CHART_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingService.generate(
                USER_ID, new CreateReadingRequest(CHART_ID, "day")))
                .isInstanceOf(ChartNotFoundException.class);

        verifyNoInteractions(astroEngineClient, gemmaClient, promptBuilder);
    }
}
