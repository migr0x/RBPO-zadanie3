package org.example.lab3.repositories;

import org.example.lab3.models.EditorialReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EditorialReviewRepository extends JpaRepository<EditorialReview, Long> {
    boolean existsByArticleId(Long articleId);
}
