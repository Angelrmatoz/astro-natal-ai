package org.astronatalai.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.astronatalai.backend.entity.BirthChart;
import org.astronatalai.backend.entity.User;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    void includesChartTransitsRangeAndDisclaimer() {
        JsonNode chartData = JsonMapper.builder().build().readTree("""
                {"ascendant": "Aries", "planets": [{"name": "Sun", "sign": "Taurus"}]}
                """);
        JsonNode transits = JsonMapper.builder().build().readTree("""
                {"planets": [{"name": "Mars", "sign": "Leo"}],
                 "aspects": [{"transit": "Mars", "natal": "Sun", "type": "square"}]}
                """);
        BirthChart chart = new BirthChart(new User("ana@example.com", "hash", "Ana"));
        chart.update(
                "Carta",
                LocalDate.of(1990, 5, 1),
                LocalTime.of(14, 30),
                true,
                "Santo Domingo",
                18.47186,
                -69.89232,
                "America/Santo_Domingo",
                chartData);

        String prompt = promptBuilder.build(
                chart, transits, ReadingType.DAY, ReadingType.DAY.range(LocalDate.of(2026, 11, 14)));

        assertThat(prompt)
                .contains("Sun", "Aries")
                .contains("Mars", "square")
                .contains("day", "2026-11-14")
                .contains("consejo médico, financiero ni legal");
    }

    @Test
    void buildsNatalPromptWithSectionsAndTimeCaveat() {
        JsonNode chartData = JsonMapper.builder().build().readTree("""
                {"ascendant": null, "planets": [{"name": "Sol", "sign": "Tauro"}]}
                """);
        BirthChart chart = new BirthChart(new User("ana@example.com", "hash", "Ana"));
        chart.update(
                "Carta",
                LocalDate.of(1990, 5, 1),
                null,
                false,
                "Santo Domingo",
                18.47186,
                -69.89232,
                "America/Santo_Domingo",
                chartData);

        String prompt = promptBuilder.build(
                chart, null, ReadingType.NATAL, ReadingType.NATAL.range(LocalDate.of(1990, 5, 1)));

        assertThat(prompt)
                .contains("carta natal completa")
                .contains("Sol", "Tauro")
                .contains("conjunciones", "oposiciones", "orbe")
                .contains("hora de nacimiento es desconocida")
                .contains("600-900 palabras")
                .contains("No uses Markdown")
                .contains("consejo médico, financiero ni legal");
    }
}
