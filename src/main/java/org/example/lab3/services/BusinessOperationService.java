package org.example.lab3.services;

import org.example.lab3.dto.ReviewArticleRequest;
import org.example.lab3.models.Article;
import org.example.lab3.models.ArticleStatus;
import org.example.lab3.models.EditionIssue;
import org.example.lab3.models.EditorialReview;
import org.example.lab3.models.IssueStatus;
import org.example.lab3.models.ReviewDecision;
import org.example.lab3.repositories.ArticleRepository;
import org.example.lab3.repositories.EditionIssueRepository;
import org.example.lab3.repositories.EditorialReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BusinessOperationService {
    private final ArticleService articleService;
    private final EditionIssueService issueService;
    private final ArticleRepository articleRepository;
    private final EditionIssueRepository issueRepository;
    private final EditorialReviewRepository reviewRepository;

    public BusinessOperationService(
            ArticleService articleService,
            EditionIssueService issueService,
            ArticleRepository articleRepository,
            EditionIssueRepository issueRepository,
            EditorialReviewRepository reviewRepository) {
        this.articleService = articleService;
        this.issueService = issueService;
        this.articleRepository = articleRepository;
        this.issueRepository = issueRepository;
        this.reviewRepository = reviewRepository;
    }

    /** Переводит готовый черновик в очередь редактора. */
    @Transactional
    public Article submitArticle(Long articleId) {
        Article article = articleService.findById(articleId);
        if (article.getStatus() != ArticleStatus.DRAFT) {
            throw new IllegalStateException("На проверку можно отправить только черновик");
        }
        article.setStatus(ArticleStatus.SUBMITTED);
        return article;
    }

    /** Создаёт рецензию и синхронно меняет статус связанной статьи. */
    @Transactional
    public EditorialReview reviewArticle(Long articleId, ReviewArticleRequest request) {
        Article article = articleService.findById(articleId);
        if (article.getStatus() != ArticleStatus.SUBMITTED) {
            throw new IllegalStateException("Проверять можно только статью, отправленную в редакцию");
        }
        if (reviewRepository.existsByArticleId(articleId)) {
            throw new IllegalStateException("Для этой статьи уже существует редакторская проверка");
        }

        EditorialReview review = new EditorialReview();
        review.setArticle(article);
        review.setDecision(request.decision());
        review.setNotes(request.notes().trim());
        review.setReviewedAt(LocalDateTime.now());
        reviewRepository.save(review);

        article.setStatus(request.decision() == ReviewDecision.APPROVED
                ? ArticleStatus.APPROVED
                : ArticleStatus.REJECTED);
        return review;
    }

    /** Добавляет одобренную статью в запланированный выпуск. */
    @Transactional
    public EditionIssue addArticleToIssue(Long issueId, Long articleId) {
        EditionIssue issue = issueService.findById(issueId);
        Article article = articleService.findById(articleId);
        ensurePlanned(issue);
        if (article.getStatus() != ArticleStatus.APPROVED) {
            throw new IllegalStateException("В выпуск можно включить только одобренную статью");
        }
        if (!article.getSection().isAvailableForPublication()) {
            throw new IllegalStateException("Рубрика статьи недоступна для публикации");
        }
        if (issue.getArticles().contains(article)) {
            throw new IllegalStateException("Статья уже добавлена в этот выпуск");
        }
        if (issueRepository.existsByArticlesIdAndStatusIn(
                articleId, List.of(IssueStatus.PLANNED, IssueStatus.PUBLISHED))) {
            throw new IllegalStateException("Статья уже зарезервирована в другом выпуске");
        }
        issue.getArticles().add(article);
        return issue;
    }

    /** Публикует выпуск и все его одобренные статьи в одной транзакции. */
    @Transactional
    public EditionIssue publishIssue(Long issueId) {
        EditionIssue issue = issueService.findById(issueId);
        ensurePlanned(issue);
        if (issue.getArticles().isEmpty()) {
            throw new IllegalStateException("Нельзя опубликовать пустой выпуск");
        }
        for (Article article : issue.getArticles()) {
            if (article.getStatus() != ArticleStatus.APPROVED) {
                throw new IllegalStateException("Все статьи выпуска должны быть одобрены");
            }
            if (!article.getSection().isAvailableForPublication()) {
                throw new IllegalStateException("Рубрика статьи недоступна для публикации");
            }
        }
        for (Article article : issue.getArticles()) {
            article.setStatus(ArticleStatus.PUBLISHED);
        }
        issue.setStatus(IssueStatus.PUBLISHED);
        return issue;
    }

    /** Отменяет запланированный выпуск и освобождает его статьи для другого выпуска. */
    @Transactional
    public EditionIssue cancelIssue(Long issueId) {
        EditionIssue issue = issueService.findById(issueId);
        ensurePlanned(issue);
        issue.getArticles().clear();
        issue.setStatus(IssueStatus.CANCELLED);
        return issue;
    }

    private void ensurePlanned(EditionIssue issue) {
        if (issue.getStatus() != IssueStatus.PLANNED) {
            throw new IllegalStateException("Операция доступна только для запланированного выпуска");
        }
    }
}
