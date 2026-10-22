package org.example.lab3.services;

import org.example.lab3.dto.ReviewPatchRequest;
import org.example.lab3.dto.ReviewRequest;
import org.example.lab3.models.Article;
import org.example.lab3.models.EditorialReview;
import org.example.lab3.repositories.EditorialReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EditorialReviewService {
    private final EditorialReviewRepository reviewRepository;
    private final ArticleService articleService;

    public EditorialReviewService(
            EditorialReviewRepository reviewRepository,
            ArticleService articleService) {
        this.reviewRepository = reviewRepository;
        this.articleService = articleService;
    }

    @Transactional(readOnly = true)
    public List<EditorialReview> findAll() {
        return reviewRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EditorialReview findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Проверка с ID " + id + " не найдена"));
    }

    @Transactional
    public EditorialReview create(ReviewRequest request) {
        EditorialReview review = new EditorialReview();
        applyAll(review, request);
        review.setReviewedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    @Transactional
    public EditorialReview replace(Long id, ReviewRequest request) {
        EditorialReview review = findById(id);
        applyAll(review, request);
        review.setReviewedAt(LocalDateTime.now());
        return review;
    }

    @Transactional
    public EditorialReview patch(Long id, ReviewPatchRequest request) {
        EditorialReview review = findById(id);
        if (request.articleId() != null) {
            review.setArticle(articleService.findById(request.articleId()));
        }
        if (request.decision() != null) {
            review.setDecision(request.decision());
        }
        if (request.notes() != null) {
            review.setNotes(normalize(request.notes()));
        }
        review.setReviewedAt(LocalDateTime.now());
        return review;
    }

    @Transactional
    public void delete(Long id) {
        reviewRepository.delete(findById(id));
    }

    private void applyAll(EditorialReview review, ReviewRequest request) {
        Article article = articleService.findById(request.articleId());
        review.setArticle(article);
        review.setDecision(request.decision());
        review.setNotes(normalize(request.notes()));
    }

    private String normalize(String notes) {
        String normalized = notes.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Комментарий редактора не должен состоять только из пробелов");
        }
        return normalized;
    }
}
