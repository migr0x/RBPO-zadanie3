package ru.mtusi.rbpo.lab3.dto;

import ru.mtusi.rbpo.lab3.model.ArticleStatus;

public record ArticleRequest(Long sectionId, Long authorId, String title, String text, ArticleStatus status) {
}
