package org.example.lab3.dto;

import jakarta.validation.constraints.Size;

public record SectionPatchRequest(
        @Size(min = 1, max = 100) String name,
        Boolean availableForPublication) {
}
