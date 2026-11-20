package org.astronatalai.backend.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.astronatalai.backend.entity.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingRepository extends JpaRepository<Reading, UUID> {

    Optional<Reading> findByChartIdAndTipoAndRangoInicio(
            UUID chartId, String tipo, LocalDate rangoInicio);
}
