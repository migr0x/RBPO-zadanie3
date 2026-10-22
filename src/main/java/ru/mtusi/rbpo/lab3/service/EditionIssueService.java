package ru.mtusi.rbpo.lab3.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtusi.rbpo.lab3.dto.EditionIssueRequest;
import ru.mtusi.rbpo.lab3.exception.ResourceConflictException;
import ru.mtusi.rbpo.lab3.exception.ResourceNotFoundException;
import ru.mtusi.rbpo.lab3.model.Article;
import ru.mtusi.rbpo.lab3.model.ArticleStatus;
import ru.mtusi.rbpo.lab3.model.EditionIssue;
import ru.mtusi.rbpo.lab3.model.EditionStatus;
import ru.mtusi.rbpo.lab3.repository.ArticleRepository;
import ru.mtusi.rbpo.lab3.repository.EditionIssueRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EditionIssueService {

    private final EditionIssueRepository repository;
    private final ArticleRepository articleRepository;

    public EditionIssueService(EditionIssueRepository repository, ArticleRepository articleRepository) {
        this.repository = repository;
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public List<EditionIssue> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public EditionIssue findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Edition issue", id));
    }

    @Transactional
    public EditionIssue create(EditionIssueRequest request) {
        if (request.date() == null) throw new IllegalArgumentException("date is required");
        List<Article> articles = resolveArticles(request.articleIds());
        EditionStatus status = request.status() == null ? EditionStatus.DRAFT : request.status();
        if (status != EditionStatus.DRAFT) {
            throw new IllegalArgumentException("A new issue must start in DRAFT status");
        }
        return repository.save(new EditionIssue(request.date(), articles, status));
    }

    @Transactional
    public EditionIssue update(Long id, EditionIssueRequest request) {
        EditionIssue current = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Edition issue", id));
        if (current.getStatus() == EditionStatus.PUBLISHED) {
            throw new IllegalArgumentException("A published issue cannot be edited");
        }
        LocalDate date = request.date() == null ? current.getDate() : request.date();
        List<Article> articles = request.articleIds() == null
                ? current.getArticles() : resolveArticles(request.articleIds());
        EditionStatus status = request.status() == null ? current.getStatus() : request.status();
        if (status != current.getStatus()) {
            throw new ResourceConflictException(
                    "Use a business operation to change the issue status");
        }
        current.setDate(date);
        current.setArticles(articles);
        current.setStatus(status);
        return current;
    }

    @Transactional
    public void deleteById(Long id) {
        EditionIssue issue = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Edition issue", id));
        if (issue.getStatus() == EditionStatus.PUBLISHED) {
            throw new ResourceConflictException("A published issue cannot be deleted");
        }
        repository.delete(issue);
    }

    private List<Article> resolveArticles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        if (ids.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("articleIds must not contain null values");
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException("articleIds must not contain duplicates");
        }
        List<Article> result = new ArrayList<>();
        for (Long id : ids) {
            Article article = articleRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Article", id));
            if (article.getStatus() != ArticleStatus.APPROVED) {
                throw new IllegalArgumentException("Only approved articles can be added to a draft issue");
            }
            result.add(article);
        }
        return result;
    }
}
