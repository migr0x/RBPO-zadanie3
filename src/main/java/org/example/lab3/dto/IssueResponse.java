package org.example.lab3.dto;

import org.example.lab3.models.IssueStatus;

import java.time.LocalDate;
import java.util.Set;

public record IssueResponse(
        Long id,
        LocalDate issueDate,
        IssueStatus status,
        Set<Long> articleIds) {
}
