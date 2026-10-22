package ru.mtusi.rbpo.lab3.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtusi.rbpo.lab3.dto.ArticleRequest;
import ru.mtusi.rbpo.lab3.exception.ResourceConflictException;
import ru.mtusi.rbpo.lab3.exception.ResourceNotFoundException;
import ru.mtusi.rbpo.lab3.model.Article;
import ru.mtusi.rbpo.lab3.model.ArticleStatus;
import ru.mtusi.rbpo.lab3.model.EditorialSection;
import ru.mtusi.rbpo.lab3.model.User;
import ru.mtusi.rbpo.lab3.repository.ArticleRepository;
import ru.mtusi.rbpo.lab3.repository.EditorialSectionRepository;
import ru.mtusi.rbpo.lab3.repository.EditorialReviewRepository;
import ru.mtusi.rbpo.lab3.repository.EditionIssueRepository;
import ru.mtusi.rbpo.lab3.repository.UserRepository;

import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository repository;
    private final EditorialSectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final EditorialReviewRepository reviewRepository;
    private final EditionIssueRepository issueRepository;

    public ArticleService(ArticleRepository repository,
                          EditorialSectionRepository sectionRepository,
                          UserRepository userRepository,
                          EditorialReviewRepository reviewRepository,
                          EditionIssueRepository issueRepository) {
        this.repository = repository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.issueRepository = issueRepository;
    }

    @Transactional(readOnly = true)
    public List<Article> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Article findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Article", id));
    }

    @Transactional
    public Article create(ArticleRequest request) {
        if (request.sectionId() == null) throw new IllegalArgumentException("sectionId is required");
        if (request.authorId() == null) throw new IllegalArgumentException("authorId is required");
        EditorialSection section = requireSection(request.sectionId());
        User author = requireUser(request.authorId());
        requireText(request.title(), "title");
        requireText(request.text(), "text");
        ArticleStatus status = request.status() == null ? ArticleStatus.DRAFT : request.status();
        if (status != ArticleStatus.DRAFT) {
            throw new IllegalArgumentException("A new article must start in DRAFT status");
        }
        validatePublicationAvailability(section, status);
        return repository.save(new Article(section, author, request.title().trim(), request.text().trim(), status));
    }

    @Transactional
    public Article update(Long id, ArticleRequest request) {
        Article current = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", id));
        if (current.getStatus() == ArticleStatus.PUBLISHED) {
            throw new ResourceConflictException("A published article cannot be edited");
        }
        EditorialSection section = request.sectionId() == null
                ? current.getSection() : requireSection(request.sectionId());
        User author = request.authorId() == null ? current.getAuthor() : requireUser(request.authorId());
        String title = request.title() == null ? current.getTitle() : request.title().trim();
        String text = request.text() == null ? current.getText() : request.text().trim();
        ArticleStatus status = request.status() == null ? current.getStatus() : request.status();
        if (status != current.getStatus()) {
            throw new ResourceConflictException("Use a business operation to change the article status");
        }
        if (request.title() != null) requireText(request.title(), "title");
        if (request.text() != null) requireText(request.text(), "text");
        validatePublicationAvailability(section, status);
        current.setSection(section);
        current.setAuthor(author);
        current.setTitle(title);
        current.setText(text);
        current.setStatus(status);
        return current;
    }

    @Transactional
    public void deleteById(Long id) {
        Article article = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Article", id));
        if (article.getStatus() == ArticleStatus.PUBLISHED) {
            throw new ResourceConflictException("A published article cannot be deleted");
        }
        reviewRepository.deleteAllByArticle_Id(id);
        for (var issue : issueRepository.findAll()) {
            if (issue.getArticles().stream().anyMatch(linked -> linked.getId().equals(id))) {
                issue.setArticles(issue.getArticles().stream()
                        .filter(linked -> !linked.getId().equals(id))
                        .toList());
            }
        }
        repository.delete(article);
    }

    private EditorialSection requireSection(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Editorial section", id));
    }

    private User requireUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void validatePublicationAvailability(EditorialSection section, ArticleStatus status) {
        if (status == ArticleStatus.PUBLISHED && !section.isPublicationAvailable()) {
            throw new IllegalArgumentException("The selected editorial section is not available for publication");
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
