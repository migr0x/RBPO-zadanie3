package org.example.lab3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.lab3.models.ReviewDecision;

public record ReviewArticleRequest(
        @NotNull ReviewDecision decision,
        @NotBlank @Size(max = 1000) String notes) {
}
