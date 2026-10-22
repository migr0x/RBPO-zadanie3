package org.example.lab3.dto;

import org.example.lab3.models.ReviewDecision;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long articleId,
        ReviewDecision decision,
        String notes,
        LocalDateTime reviewedAt) {
}
