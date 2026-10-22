package org.example.lab3.repositories;

import org.example.lab3.models.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByAuthorId(Long authorId);

    boolean existsBySectionId(Long sectionId);
}
