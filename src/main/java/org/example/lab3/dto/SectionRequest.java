package org.example.lab3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SectionRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Boolean availableForPublication) {
}
