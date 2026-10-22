package org.example.lab3.services;

import org.example.lab3.dto.ArticlePatchRequest;
import org.example.lab3.dto.ArticleRequest;
import org.example.lab3.models.Article;
import org.example.lab3.models.ArticleStatus;
import org.example.lab3.models.EditorialSection;
import org.example.lab3.models.EditorialUser;
import org.example.lab3.models.UserRole;
import org.example.lab3.repositories.ArticleRepository;
import org.example.lab3.repositories.EditionIssueRepository;
import org.example.lab3.repositories.EditorialReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final EditorialReviewRepository reviewRepository;
    private final EditionIssueRepository issueRepository;
    private final EditorialSectionService sectionService;
    private final EditorialUserService userService;

    public ArticleService(
            ArticleRepository articleRepository,
            EditorialReviewRepository reviewRepository,
            EditionIssueRepository issueRepository,
            EditorialSectionService sectionService,
            EditorialUserService userService) {
        this.articleRepository = articleRepository;
        this.reviewRepository = reviewRepository;
        this.issueRepository = issueRepository;
        this.sectionService = sectionService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Статья с ID " + id + " не найдена"));
    }

    @Transactional
    public Article create(ArticleRequest request) {
        Article article = new Article();
        applyAll(article, request);
        article.setStatus(ArticleStatus.DRAFT);
        return articleRepository.save(article);
    }

    @Transactional
    public Article replace(Long id, ArticleRequest request) {
        Article article = findById(id);
        ensureEditable(article);
        applyAll(article, request);
        return article;
    }

    @Transactional
    public Article patch(Long id, ArticlePatchRequest request) {
        Article article = findById(id);
        ensureEditable(article);
        if (request.sectionId() != null) {
            article.setSection(sectionService.findById(request.sectionId()));
        }
        if (request.authorId() != null) {
            article.setAuthor(resolveAuthor(request.authorId()));
        }
        if (request.title() != null) {
            article.setTitle(normalize(request.title(), "Заголовок"));
        }
        if (request.text() != null) {
            article.setText(normalize(request.text(), "Текст"));
        }
        return article;
    }

    @Transactional
    public void delete(Long id) {
        Article article = findById(id);
        if (reviewRepository.existsByArticleId(id)) {
            throw new IllegalStateException("Нельзя удалить статью с редакторской проверкой");
        }
        if (issueRepository.existsByArticlesId(id)) {
            throw new IllegalStateException("Нельзя удалить статью, добавленную в выпуск");
        }
        articleRepository.delete(article);
    }

    private void applyAll(Article article, ArticleRequest request) {
        EditorialSection section = sectionService.findById(request.sectionId());
        EditorialUser author = resolveAuthor(request.authorId());
        article.setSection(section);
        article.setAuthor(author);
        article.setTitle(normalize(request.title(), "Заголовок"));
        article.setText(normalize(request.text(), "Текст"));
    }

    private EditorialUser resolveAuthor(Long authorId) {
        EditorialUser author = userService.findById(authorId);
        if (author.getRole() != UserRole.AUTHOR) {
            throw new IllegalArgumentException("Автором статьи может быть только пользователь с ролью AUTHOR");
        }
        return author;
    }

    private void ensureEditable(Article article) {
        if (article.getStatus() == ArticleStatus.PUBLISHED) {
            throw new IllegalStateException("Опубликованную статью нельзя редактировать");
        }
    }

    private String normalize(String value, String field) {
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " не должен состоять только из пробелов");
        }
        return normalized;
    }
}
