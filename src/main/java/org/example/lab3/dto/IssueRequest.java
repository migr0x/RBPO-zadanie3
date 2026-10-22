package org.example.lab3.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record IssueRequest(
        @NotNull LocalDate issueDate,
        Set<Long> articleIds) {
}
