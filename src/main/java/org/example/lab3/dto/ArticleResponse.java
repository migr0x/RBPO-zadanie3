package org.example.lab3.dto;

import org.example.lab3.models.ArticleStatus;

public record ArticleResponse(
        Long id,
        Long sectionId,
        Long authorId,
        String title,
        String text,
        ArticleStatus status) {
}
