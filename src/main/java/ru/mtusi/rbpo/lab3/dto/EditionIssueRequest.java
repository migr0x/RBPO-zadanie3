package ru.mtusi.rbpo.lab3.dto;

import ru.mtusi.rbpo.lab3.model.EditionStatus;

import java.time.LocalDate;
import java.util.List;

public record EditionIssueRequest(LocalDate date, List<Long> articleIds, EditionStatus status) {
}
