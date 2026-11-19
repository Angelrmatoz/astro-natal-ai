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
        return type == ReadingType.NATAL
                ? buildNatal(chart)
                : buildForecast(chart, transits, type, range);
    }

    private String buildNatal(BirthChart chart) {
        String hora = chart.getHoraNac() == null ? "desconocida" : chart.getHoraNac().toString();
        String casas = chart.isTimeKnown()
                ? "La hora de nacimiento es conocida: incluye ascendente, medio cielo y casas."
                : "La hora de nacimiento es desconocida: no hay ascendente ni casas. No los inventes ni los menciones.";

        return """
                Eres un astrólogo profesional. Interpreta la carta natal completa de esta persona.

                Datos de nacimiento:
                - Fecha: %s
                - Hora: %s
                - Ciudad: %s
                - Zona horaria: %s

                Carta natal ya calculada (no inventes ni cambies estas posiciones):
                %s

                %s

                Estructura la lectura en estas secciones:
                1. Sol, Luna y ascendente (si existe): el núcleo de la personalidad.
                2. Planetas por signo y, si hay casas, por casa.
                3. Aspectos principales: conjunciones, oposiciones, trígonos, cuadraturas y sextiles,
                   con su orbe y lo que muestran juntos.
                4. Síntesis: tres o cuatro ideas que integren la carta.

                Usa terminología astrológica precisa y en español. Explica cada término técnico
                la primera vez que aparece. No fatalismos, ni diagnósticos, ni predicciones absolutas.
                Extensión: 600-900 palabras.
                No uses Markdown: sin encabezados con #, sin asteriscos ni negritas.
                Separa las secciones con una línea en blanco.
                Incluye un aviso breve: esta interpretación es astrológica y no sustituye
                consejo médico, financiero ni legal.
                Responde únicamente con la lectura en español.
                """.formatted(
                chart.getFechaNac(),
                hora,
                chart.getCiudad(),
                chart.getTimezone(),
                chart.getDatosCarta(),
                casas);
    }

    private String buildForecast(BirthChart chart,
                                 JsonNode transits,
                                 ReadingType type,
                                 ReadingType.Range range) {
        String length = switch (type) {
            case DAY -> "2-3 párrafos (200-300 palabras)";
            case WEEK -> "4-6 párrafos (400-600 palabras)";
            case MONTH -> "6-8 párrafos (600-900 palabras)";
            case NATAL -> throw new IllegalStateException("NATAL usa la plantilla de carta natal");
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
