package ru.mtusi.rbpo.lab3.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtusi.rbpo.lab3.dto.UserRequest;
import ru.mtusi.rbpo.lab3.exception.ResourceConflictException;
import ru.mtusi.rbpo.lab3.exception.ResourceNotFoundException;
import ru.mtusi.rbpo.lab3.model.Article;
import ru.mtusi.rbpo.lab3.model.ArticleStatus;
import ru.mtusi.rbpo.lab3.model.User;
import ru.mtusi.rbpo.lab3.repository.UserRepository;
import ru.mtusi.rbpo.lab3.repository.ArticleRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;
    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

    public UserService(UserRepository repository, ArticleRepository articleRepository, ArticleService articleService) {
        this.repository = repository;
        this.articleRepository = articleRepository;
        this.articleService = articleService;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional
    public User create(UserRequest request) {
        requireText(request.username(), "username");
        requireText(request.displayName(), "displayName");
        String username = request.username().trim();
        String email = normalizeEmail(request.email());
        ensureUnique(username, email, null);
        return repository.save(new User(username, request.displayName().trim(), email));
    }

    @Transactional
    public User update(Long id, UserRequest request) {
        User current = findById(id);
        String username = request.username() == null ? current.getUsername() : request.username().trim();
        String displayName = request.displayName() == null ? current.getDisplayName() : request.displayName().trim();
        String email = request.email() == null ? current.getEmail() : normalizeEmail(request.email());
        if (request.username() != null) requireText(request.username(), "username");
        if (request.displayName() != null) requireText(request.displayName(), "displayName");
        ensureUnique(username, email, id);
        current.setUsername(username);
        current.setDisplayName(displayName);
        current.setEmail(email);
        return current;
    }

    @Transactional
    public void deleteById(Long id) {
        User user = findById(id);
        List<Article> authoredArticles = articleRepository.findByAuthor_Id(id);
        if (authoredArticles.stream().anyMatch(article -> article.getStatus() == ArticleStatus.PUBLISHED)) {
            throw new ResourceConflictException("A user with published articles cannot be deleted");
        }
        authoredArticles.stream().map(article -> article.getId()).forEach(articleService::deleteById);
        repository.delete(user);
    }

    private void ensureUnique(String username, String email, Long currentId) {
        boolean usernameTaken = repository.findAll().stream()
                .anyMatch(user -> !user.getId().equals(currentId)
                        && user.getUsername().equalsIgnoreCase(username));
        if (usernameTaken) {
            throw new ResourceConflictException("username is already in use");
        }
        if (email != null) {
            boolean emailTaken = repository.findAll().stream()
                    .anyMatch(user -> !user.getId().equals(currentId)
                            && user.getEmail() != null && user.getEmail().equalsIgnoreCase(email));
            if (emailTaken) {
                throw new ResourceConflictException("email is already in use");
            }
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalized = email.trim();
        if (!normalized.contains("@")) {
            throw new IllegalArgumentException("email must contain @");
        }
        return normalized;
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
