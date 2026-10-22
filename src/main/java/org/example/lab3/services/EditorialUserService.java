package org.example.lab3.services;

import org.example.lab3.dto.UserPatchRequest;
import org.example.lab3.dto.UserRequest;
import org.example.lab3.models.EditorialUser;
import org.example.lab3.repositories.ArticleRepository;
import org.example.lab3.repositories.EditorialUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EditorialUserService {
    private final EditorialUserRepository userRepository;
    private final ArticleRepository articleRepository;

    public EditorialUserService(
            EditorialUserRepository userRepository,
            ArticleRepository articleRepository) {
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public List<EditorialUser> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EditorialUser findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + id + " не найден"));
    }

    @Transactional
    public EditorialUser create(UserRequest request) {
        EditorialUser user = new EditorialUser();
        user.setName(normalize(request.name()));
        user.setRole(request.role());
        return userRepository.save(user);
    }

    @Transactional
    public EditorialUser replace(Long id, UserRequest request) {
        EditorialUser user = findById(id);
        user.setName(normalize(request.name()));
        user.setRole(request.role());
        return user;
    }

    @Transactional
    public EditorialUser patch(Long id, UserPatchRequest request) {
        EditorialUser user = findById(id);
        if (request.name() != null) {
            user.setName(normalize(request.name()));
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        return user;
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        if (articleRepository.existsByAuthorId(id)) {
            throw new IllegalStateException("Нельзя удалить пользователя: он является автором статьи");
        }
        userRepository.deleteById(id);
    }

    private String normalize(String value) {
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Значение не должно состоять только из пробелов");
        }
        return normalized;
    }
}
