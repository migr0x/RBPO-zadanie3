package org.example.lab3.dto;

import java.time.LocalDate;
import java.util.Set;

public record IssuePatchRequest(LocalDate issueDate, Set<Long> articleIds) {
}
