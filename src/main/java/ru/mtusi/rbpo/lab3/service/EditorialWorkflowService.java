package ru.mtusi.rbpo.lab3.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtusi.rbpo.lab3.dto.EditorialDecisionResult;
import ru.mtusi.rbpo.lab3.exception.ResourceConflictException;
import ru.mtusi.rbpo.lab3.exception.ResourceNotFoundException;
import ru.mtusi.rbpo.lab3.model.Article;
import ru.mtusi.rbpo.lab3.model.ArticleStatus;
import ru.mtusi.rbpo.lab3.model.EditionIssue;
import ru.mtusi.rbpo.lab3.model.EditionStatus;
import ru.mtusi.rbpo.lab3.model.EditorialReview;
import ru.mtusi.rbpo.lab3.model.ReviewDecision;
import ru.mtusi.rbpo.lab3.repository.ArticleRepository;
import ru.mtusi.rbpo.lab3.repository.EditionIssueRepository;
import ru.mtusi.rbpo.lab3.repository.EditorialReviewRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EditorialWorkflowService {

    private final ArticleRepository articleRepository;
    private final EditorialReviewRepository reviewRepository;
    private final EditionIssueRepository issueRepository;
    private final Clock clock;

    public EditorialWorkflowService(ArticleRepository articleRepository,
                                    EditorialReviewRepository reviewRepository,
                                    EditionIssueRepository issueRepository,
                                    Clock clock) {
        this.articleRepository = articleRepository;
        this.reviewRepository = reviewRepository;
        this.issueRepository = issueRepository;
        this.clock = clock;
    }

    @Transactional
    public Article submitForReview(Long articleId) {
        Article article = lockArticle(articleId);
        requireStatus(article, ArticleStatus.DRAFT, "Only a draft article can be submitted for editorial review");
        article.setStatus(ArticleStatus.IN_REVIEW);
        return article;
    }

    @Transactional
    public EditorialDecisionResult approveArticle(Long articleId, String remarks) {
        Article article = lockArticle(articleId);
        requireStatus(article, ArticleStatus.IN_REVIEW, "Only an article under review can be approved");
        EditorialReview review = new EditorialReview(article, ReviewDecision.APPROVED,
                normalizeRemarks(remarks), Instant.now(clock));
        article.setStatus(ArticleStatus.APPROVED);
        articleRepository.save(article);
        return new EditorialDecisionResult(article, reviewRepository.save(review));
    }

    @Transactional
    public EditorialDecisionResult requestRevision(Long articleId, String remarks) {
        Article article = lockArticle(articleId);
        requireStatus(article, ArticleStatus.IN_REVIEW, "Only an article under review can be returned for revision");
        EditorialReview review = new EditorialReview(article, ReviewDecision.NEEDS_REVISION,
                normalizeRemarks(remarks), Instant.now(clock));
        article.setStatus(ArticleStatus.DRAFT);
        articleRepository.save(article);
        return new EditorialDecisionResult(article, reviewRepository.save(review));
    }

    @Transactional
    public EditionIssue includeArticle(Long issueId, Long articleId) {
        EditionIssue issue = lockIssue(issueId);
        if (issue.getStatus() != EditionStatus.DRAFT) {
            throw new ResourceConflictException("Articles can be added only to a draft issue");
        }
        Article article = lockArticle(articleId);
        if (article.getStatus() != ArticleStatus.APPROVED) {
            throw new ResourceConflictException("Only approved articles can be included in a draft issue");
        }
        if (issue.getArticles().stream().anyMatch(existing -> existing.getId().equals(articleId))) {
            throw new ResourceConflictException("The article is already included in this issue");
        }
        ArrayList<Article> articles = new ArrayList<>(issue.getArticles());
        articles.add(article);
        issue.setArticles(articles);
        return issue;
    }

    @Transactional
    public EditionIssue publishIssue(Long issueId) {
        EditionIssue issue = lockIssue(issueId);
        if (issue.getStatus() != EditionStatus.DRAFT) {
            throw new ResourceConflictException("Only a draft issue can be published");
        }
        if (issue.getArticles().isEmpty()) {
            throw new ResourceConflictException("An issue must contain at least one article before publication");
        }
        for (Article linkedArticle : new ArrayList<>(issue.getArticles())) {
            Article article = lockArticle(linkedArticle.getId());
            if (article.getStatus() != ArticleStatus.APPROVED) {
                throw new ResourceConflictException("Every issue article must be approved and not already published");
            }
            if (!article.getSection().isPublicationAvailable()) {
                throw new ResourceConflictException("An article belongs to a section unavailable for publication");
            }
            if (issueRepository.existsPublishedContainingArticle(article.getId(), EditionStatus.PUBLISHED)) {
                throw new ResourceConflictException("An article cannot be published in more than one issue");
            }
            article.setStatus(ArticleStatus.PUBLISHED);
        }
        issue.setStatus(EditionStatus.PUBLISHED);
        List<Long> publishedArticleIds = issue.getArticles().stream().map(Article::getId).toList();
        for (EditionIssue otherIssue : issueRepository.findAll()) {
            if (!otherIssue.getId().equals(issueId) && otherIssue.getStatus() == EditionStatus.DRAFT) {
                List<Article> remainingArticles = otherIssue.getArticles().stream()
                        .filter(article -> !publishedArticleIds.contains(article.getId()))
                        .toList();
                if (remainingArticles.size() != otherIssue.getArticles().size()) {
                    otherIssue.setArticles(remainingArticles);
                }
            }
        }
        return issue;
    }

    private Article lockArticle(Long id) {
        return articleRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", id));
    }

    private EditionIssue lockIssue(Long id) {
        return issueRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Edition issue", id));
    }

    private void requireStatus(Article article, ArticleStatus expected, String message) {
        if (article.getStatus() != expected) {
            throw new ResourceConflictException(message);
        }
    }

    private String normalizeRemarks(String remarks) {
        return remarks == null || remarks.isBlank() ? null : remarks.trim();
    }
}
