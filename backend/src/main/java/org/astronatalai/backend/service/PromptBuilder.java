package org.astronatalai.backend.service;

import org.astronatalai.backend.entity.BirthChart;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class PromptBuilder {

    public String build(BirthChart chart,
                        JsonNode transits,
                        ReadingType type,
                        ReadingType.Range range) {
        String length = switch (type) {
            case DAY -> "2-3 párrafos (200-300 palabras)";
            case WEEK -> "4-6 párrafos (400-600 palabras)";
            case MONTH -> "6-8 párrafos (600-900 palabras)";
        };
        String hora = chart.getHoraNac() == null ? "desconocida" : chart.getHoraNac().toString();

        return """
                Eres un astrólogo profesional. Interpreta la siguiente carta natal y los tránsitos
                actuales para generar una lectura personalizada.

                Datos de nacimiento:
                - Fecha: %s
                - Hora: %s
                - Hora conocida: %s
                - Ciudad: %s
                - Zona horaria: %s

                Carta natal ya calculada (no inventes ni cambies estas posiciones):
                %s

                Tránsitos actuales ya calculados:
                %s

                Genera una lectura de tipo %s para el rango %s a %s.
                Extensión: %s.
                Tono cálido, claro y responsable; evita fatalismos y predicciones absolutas.
                Relaciona los tránsitos con los planetas y casas de la carta.
                Incluye un aviso breve: esta interpretación es astrológica y no sustituye
                consejo médico, financiero ni legal.
                Responde únicamente con la lectura en español.
                """.formatted(
                chart.getFechaNac(),
                hora,
                chart.isTimeKnown(),
                chart.getCiudad(),
                chart.getTimezone(),
                chart.getDatosCarta(),
                transits,
                type.value(),
                range.start(),
                range.end(),
                length);
    }
}
