package org.astronatalai.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import org.astronatalai.backend.client.AstroEngineClient;
import org.astronatalai.backend.client.GeocodingClient;
import org.astronatalai.backend.dto.CreateChartRequest;
import org.astronatalai.backend.entity.User;
import org.astronatalai.backend.exception.InvalidChartRequestException;
import org.astronatalai.backend.repository.BirthChartRepository;
import org.astronatalai.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChartServiceTest {

    @Mock
    private BirthChartRepository birthChartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeocodingClient geocodingClient;

    @Mock
    private AstroEngineClient astroEngineClient;

    @InjectMocks
    private ChartService chartService;

    @Test
    void rejectsKnownTimeWithoutHour() {
        CreateChartRequest request = new CreateChartRequest(
                "Carta", LocalDate.of(1990, 5, 1), null, true,
                "Santo Domingo", null, null, null);

        assertThatThrownBy(() -> chartService.save(UUID.randomUUID(), request))
                .isInstanceOf(InvalidChartRequestException.class)
                .hasMessageContaining("horaNac");

        verifyNoInteractions(geocodingClient, astroEngineClient, birthChartRepository, userRepository);
    }

    @Test
    void rejectsHourWhenTimeIsUnknown() {
        CreateChartRequest request = new CreateChartRequest(
                "Carta", LocalDate.of(1990, 5, 1), LocalTime.of(14, 30), false,
                "Santo Domingo", null, null, null);

        assertThatThrownBy(() -> chartService.save(UUID.randomUUID(), request))
                .isInstanceOf(InvalidChartRequestException.class)
                .hasMessageContaining("horaNac");
    }

    @Test
    void rejectsManualCoordinatesWithoutTimezone() {
        stubUser();
        CreateChartRequest request = new CreateChartRequest(
                "Carta", LocalDate.of(1990, 5, 1), LocalTime.of(14, 30), true,
                null, 18.4861, -69.9312, null);

        assertThatThrownBy(() -> chartService.save(UUID.randomUUID(), request))
                .isInstanceOf(InvalidChartRequestException.class)
                .hasMessageContaining("timezone");

        verifyNoInteractions(geocodingClient, astroEngineClient);
    }

    @Test
    void rejectsMissingCityAndCoordinates() {
        stubUser();
        CreateChartRequest request = new CreateChartRequest(
                "Carta", LocalDate.of(1990, 5, 1), LocalTime.of(14, 30), true,
                null, null, null, null);

        assertThatThrownBy(() -> chartService.save(UUID.randomUUID(), request))
                .isInstanceOf(InvalidChartRequestException.class)
                .hasMessageContaining("ciudad");

        verifyNoInteractions(geocodingClient, astroEngineClient);
    }

    private void stubUser() {
        when(userRepository.findById(any())).thenReturn(
                Optional.of(new User("ana@example.com", "hash", "Ana")));
    }
}
