package ru.mtusi.rbpo.lab3.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.mtusi.rbpo.lab3.model.ReviewDecision;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EditorialReviewRequest(Long articleId, ReviewDecision decision, String remarks) {
}
