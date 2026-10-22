package org.example.lab3.dto;

import jakarta.validation.constraints.Size;

public record ArticlePatchRequest(
        Long sectionId,
        Long authorId,
        @Size(min = 1, max = 200) String title,
        @Size(min = 1) String text) {
}
