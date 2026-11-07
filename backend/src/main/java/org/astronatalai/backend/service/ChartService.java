package org.astronatalai.backend.service;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.client.AstroEngineClient;
import org.astronatalai.backend.client.GeocodingClient;
import org.astronatalai.backend.dto.ChartResponse;
import org.astronatalai.backend.dto.CreateChartRequest;
import org.astronatalai.backend.dto.GeocodingResult;
import org.astronatalai.backend.dto.NatalChartCommand;
import org.astronatalai.backend.entity.BirthChart;
import org.astronatalai.backend.entity.User;
import org.astronatalai.backend.exception.ChartNotFoundException;
import org.astronatalai.backend.exception.InvalidChartRequestException;
import org.astronatalai.backend.repository.BirthChartRepository;
import org.astronatalai.backend.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final BirthChartRepository birthChartRepository;
    private final UserRepository userRepository;
    private final GeocodingClient geocodingClient;
    private final AstroEngineClient astroEngineClient;

    public ChartResponse save(UUID userId, CreateChartRequest request) {
        validateTime(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
        Location location = resolveLocation(request);

        NatalChartCommand command = new NatalChartCommand(
                request.fechaNac(),
                request.horaNac(),
                request.timeKnown(),
                location.latitud(),
                location.longitud(),
                location.timezone());
        JsonNode datosCarta = astroEngineClient.natalChart(command);

        String nombre = request.nombre() == null || request.nombre().isBlank()
                ? "Carta natal"
                : request.nombre().trim();

        BirthChart chart = birthChartRepository.findByUserId(userId)
                .orElseGet(() -> new BirthChart(user));
        chart.update(
                nombre,
                request.fechaNac(),
                request.horaNac(),
                request.timeKnown(),
                location.ciudad(),
                location.latitud(),
                location.longitud(),
                location.timezone(),
                datosCarta);

        return ChartResponse.from(birthChartRepository.saveAndFlush(chart));
    }

    public ChartResponse me(UUID userId) {
        return ChartResponse.from(getByUserId(userId));
    }

    public BirthChart getByUserId(UUID userId) {
        return birthChartRepository.findByUserId(userId)
                .orElseThrow(ChartNotFoundException::new);
    }

    private static void validateTime(CreateChartRequest request) {
        LocalTime hora = request.horaNac();
        if (request.timeKnown() && hora == null) {
            throw new InvalidChartRequestException("horaNac es obligatoria cuando timeKnown=true");
        }
        if (!request.timeKnown() && hora != null) {
            throw new InvalidChartRequestException("horaNac debe ser null cuando timeKnown=false");
        }
    }

    private Location resolveLocation(CreateChartRequest request) {
        boolean hasLatitude = request.latitud() != null;
        boolean hasLongitude = request.longitud() != null;

        if (hasLatitude || hasLongitude) {
            if (!hasLatitude || !hasLongitude) {
                throw new InvalidChartRequestException("latitud y longitud deben enviarse juntas");
            }
            if (request.timezone() == null || request.timezone().isBlank()) {
                throw new InvalidChartRequestException("timezone es obligatoria con coordenadas manuales");
            }
            validateTimezone(request.timezone());
            String ciudad = request.ciudad() == null || request.ciudad().isBlank()
                    ? "Coordenadas"
                    : request.ciudad().trim();
            return new Location(ciudad, request.latitud(), request.longitud(), request.timezone());
        }

        if (request.ciudad() == null || request.ciudad().isBlank()) {
            throw new InvalidChartRequestException("ciudad o coordenadas manuales son obligatorias");
        }

        GeocodingResult geocoded = geocodingClient.geocode(request.ciudad().trim());
        validateTimezone(geocoded.timezone());
        return new Location(geocoded.ciudad(), geocoded.latitud(), geocoded.longitud(), geocoded.timezone());
    }

    private static void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            throw new InvalidChartRequestException("timezone no es una zona IANA válida: " + timezone);
        }
    }

    private record Location(String ciudad, double latitud, double longitud, String timezone) {
    }
}
