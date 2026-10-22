package org.example.lab3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ArticleRequest(
        @NotNull Long sectionId,
        @NotNull Long authorId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String text) {
}
