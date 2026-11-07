package org.astronatalai.backend.repository;

import java.util.Optional;
import java.util.UUID;
import org.astronatalai.backend.entity.BirthChart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BirthChartRepository extends JpaRepository<BirthChart, UUID> {

    Optional<BirthChart> findByUserId(UUID userId);
}
