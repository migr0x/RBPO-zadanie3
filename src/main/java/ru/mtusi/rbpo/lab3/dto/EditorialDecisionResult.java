package ru.mtusi.rbpo.lab3.dto;

import ru.mtusi.rbpo.lab3.model.Article;
import ru.mtusi.rbpo.lab3.model.EditorialReview;

public record EditorialDecisionResult(Article article, EditorialReview review) {
}
