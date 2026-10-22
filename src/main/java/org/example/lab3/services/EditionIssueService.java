package org.example.lab3.services;

import org.example.lab3.dto.IssuePatchRequest;
import org.example.lab3.dto.IssueRequest;
import org.example.lab3.models.Article;
import org.example.lab3.models.ArticleStatus;
import org.example.lab3.models.EditionIssue;
import org.example.lab3.models.IssueStatus;
import org.example.lab3.repositories.EditionIssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class EditionIssueService {
    private final EditionIssueRepository issueRepository;
    private final ArticleService articleService;

    public EditionIssueService(
            EditionIssueRepository issueRepository,
            ArticleService articleService) {
        this.issueRepository = issueRepository;
        this.articleService = articleService;
    }

    @Transactional(readOnly = true)
    public List<EditionIssue> findAll() {
        return issueRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EditionIssue findById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Выпуск с ID " + id + " не найден"));
    }

    @Transactional
    public EditionIssue create(IssueRequest request) {
        EditionIssue issue = new EditionIssue();
        issue.setIssueDate(request.issueDate());
        issue.setStatus(IssueStatus.PLANNED);
        issue.setArticles(resolveArticles(request.articleIds()));
        return issueRepository.save(issue);
    }

    @Transactional
    public EditionIssue replace(Long id, IssueRequest request) {
        EditionIssue issue = findById(id);
        ensurePlanned(issue);
        issue.setIssueDate(request.issueDate());
        issue.setArticles(resolveArticles(request.articleIds()));
        return issue;
    }

    @Transactional
    public EditionIssue patch(Long id, IssuePatchRequest request) {
        EditionIssue issue = findById(id);
        ensurePlanned(issue);
        if (request.issueDate() != null) {
            issue.setIssueDate(request.issueDate());
        }
        if (request.articleIds() != null) {
            issue.setArticles(resolveArticles(request.articleIds()));
        }
        return issue;
    }

    @Transactional
    public void delete(Long id) {
        EditionIssue issue = findById(id);
        ensurePlanned(issue);
        issueRepository.delete(issue);
    }

    private Set<Article> resolveArticles(Set<Long> articleIds) {
        Set<Article> articles = new LinkedHashSet<>();
        if (articleIds == null) {
            return articles;
        }
        for (Long articleId : articleIds) {
            Article article = articleService.findById(articleId);
            if (article.getStatus() != ArticleStatus.APPROVED) {
                throw new IllegalStateException("В выпуск можно добавить только одобренную статью с ID " + articleId);
            }
            if (!article.getSection().isAvailableForPublication()) {
                throw new IllegalStateException("Рубрика статьи с ID " + articleId + " недоступна для публикации");
            }
            articles.add(article);
        }
        return articles;
    }

    private void ensurePlanned(EditionIssue issue) {
        if (issue.getStatus() != IssueStatus.PLANNED) {
            throw new IllegalStateException("Изменять можно только запланированный выпуск");
        }
    }
}
