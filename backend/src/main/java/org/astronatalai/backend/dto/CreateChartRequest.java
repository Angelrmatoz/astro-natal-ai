package org.astronatalai.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateChartRequest(
        @Size(max = 120) String nombre,
        @NotNull @PastOrPresent LocalDate fechaNac,
        LocalTime horaNac,
        boolean timeKnown,
        @Size(max = 160) String ciudad,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitud,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitud,
        @Size(max = 64) String timezone) {
}
