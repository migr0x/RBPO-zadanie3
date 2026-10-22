package org.example.lab3.dto;

import jakarta.validation.constraints.Size;
import org.example.lab3.models.ReviewDecision;

public record ReviewPatchRequest(
        Long articleId,
        ReviewDecision decision,
        @Size(min = 1, max = 1000) String notes) {
}
