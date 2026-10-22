package ru.mtusi.rbpo.lab3.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtusi.rbpo.lab3.dto.EditorialReviewRequest;
import ru.mtusi.rbpo.lab3.exception.ResourceConflictException;
import ru.mtusi.rbpo.lab3.exception.ResourceNotFoundException;
import ru.mtusi.rbpo.lab3.model.Article;
import ru.mtusi.rbpo.lab3.model.ArticleStatus;
import ru.mtusi.rbpo.lab3.model.EditorialReview;
import ru.mtusi.rbpo.lab3.model.ReviewDecision;
import ru.mtusi.rbpo.lab3.repository.ArticleRepository;
import ru.mtusi.rbpo.lab3.repository.EditorialReviewRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class EditorialReviewService {

    private final EditorialReviewRepository repository;
    private final ArticleRepository articleRepository;
    private final Clock clock;

    public EditorialReviewService(EditorialReviewRepository repository,
                                  ArticleRepository articleRepository,
                                  Clock clock) {
        this.repository = repository;
        this.articleRepository = articleRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<EditorialReview> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public EditorialReview findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Editorial review", id));
    }

    @Transactional
    public EditorialReview create(EditorialReviewRequest request) {
        if (request.articleId() == null) throw new IllegalArgumentException("articleId is required");
        if (request.decision() == null) throw new IllegalArgumentException("decision is required");
        Article article = requireArticle(request.articleId());
        validateDecisionState(article, request.decision());
        return repository.save(new EditorialReview(article, request.decision(), request.remarks(), Instant.now(clock)));
    }

    @Transactional
    public EditorialReview update(Long id, EditorialReviewRequest request) {
        EditorialReview current = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Editorial review", id));
        if (request.articleId() != null && !request.articleId().equals(current.getArticleId())) {
            throw new ResourceConflictException("The article reference of an editorial review is immutable");
        }
        if (request.decision() != null && request.decision() != current.getDecision()) {
            throw new ResourceConflictException("The decision of an editorial review is immutable");
        }
        if (request.remarks() != null) {
            current.setRemarks(request.remarks());
            current.setReviewedAt(Instant.now(clock));
        }
        return current;
    }

    @Transactional
    public void deleteById(Long id) {
        EditorialReview review = findById(id);
        Article article = review.getArticle();
        if (review.getDecision() == ReviewDecision.APPROVED
                && (article.getStatus() == ArticleStatus.APPROVED
                || article.getStatus() == ArticleStatus.PUBLISHED)) {
            throw new ResourceConflictException("The approval record of an approved or published article cannot be deleted");
        }
        if (review.getDecision() == ReviewDecision.REJECTED
                && article.getStatus() == ArticleStatus.REJECTED) {
            throw new ResourceConflictException("The rejection record of a rejected article cannot be deleted");
        }
        repository.delete(review);
    }

    private Article requireArticle(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", articleId));
    }

    private void validateDecisionState(Article article, ReviewDecision decision) {
        boolean matches = switch (decision) {
            case APPROVED -> article.getStatus() == ArticleStatus.APPROVED
                    || article.getStatus() == ArticleStatus.PUBLISHED;
            case NEEDS_REVISION -> article.getStatus() == ArticleStatus.DRAFT;
            case REJECTED -> article.getStatus() == ArticleStatus.REJECTED;
        };
        if (!matches) {
            throw new ResourceConflictException("The review decision does not match the current article status");
        }
    }
}
