package org.astronatalai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateReadingRequest(
        @NotNull UUID chartId,
        @NotBlank
        @Size(max = 10)
        @Pattern(regexp = "day|week|month|natal", message = "type debe ser day, week, month o natal")
        String type) {
}
