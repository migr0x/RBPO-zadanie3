package org.example.lab3.config;

import org.example.lab3.dto.ArticleResponse;
import org.example.lab3.dto.IssueResponse;
import org.example.lab3.dto.ReviewResponse;
import org.example.lab3.dto.SectionResponse;
import org.example.lab3.dto.UserResponse;
import org.example.lab3.models.Article;
import org.example.lab3.models.EditionIssue;
import org.example.lab3.models.EditorialReview;
import org.example.lab3.models.EditorialSection;
import org.example.lab3.models.EditorialUser;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;

@Component
public class DtoMapper {
    public UserResponse toResponse(EditorialUser user) {
        return new UserResponse(user.getId(), user.getName(), user.getRole());
    }

    public SectionResponse toResponse(EditorialSection section) {
        return new SectionResponse(
                section.getId(), section.getName(), section.isAvailableForPublication());
    }

    public ArticleResponse toResponse(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getSection().getId(),
                article.getAuthor().getId(),
                article.getTitle(),
                article.getText(),
                article.getStatus());
    }

    public ReviewResponse toResponse(EditorialReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getArticle().getId(),
                review.getDecision(),
                review.getNotes(),
                review.getReviewedAt());
    }

    public IssueResponse toResponse(EditionIssue issue) {
        LinkedHashSet<Long> articleIds = new LinkedHashSet<>();
        for (Article article : issue.getArticles()) {
            articleIds.add(article.getId());
        }
        return new IssueResponse(issue.getId(), issue.getIssueDate(), issue.getStatus(), articleIds);
    }
}
