package ru.mtusi.rbpo.lab3.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtusi.rbpo.lab3.dto.EditorialSectionRequest;
import ru.mtusi.rbpo.lab3.exception.ResourceConflictException;
import ru.mtusi.rbpo.lab3.exception.ResourceNotFoundException;
import ru.mtusi.rbpo.lab3.model.Article;
import ru.mtusi.rbpo.lab3.model.EditorialSection;
import ru.mtusi.rbpo.lab3.model.ArticleStatus;
import ru.mtusi.rbpo.lab3.repository.EditorialSectionRepository;
import ru.mtusi.rbpo.lab3.repository.ArticleRepository;

import java.util.List;

@Service
public class EditorialSectionService {

    private final EditorialSectionRepository repository;
    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

    public EditorialSectionService(EditorialSectionRepository repository, ArticleRepository articleRepository,
                                   ArticleService articleService) {
        this.repository = repository;
        this.articleRepository = articleRepository;
        this.articleService = articleService;
    }

    @Transactional(readOnly = true)
    public List<EditorialSection> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public EditorialSection findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Editorial section", id));
    }

    @Transactional
    public EditorialSection create(EditorialSectionRequest request) {
        requireName(request.name());
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResourceConflictException("editorial section name is already in use");
        }
        boolean available = Boolean.TRUE.equals(request.publicationAvailable());
        return repository.save(new EditorialSection(name, available));
    }

    @Transactional
    public EditorialSection update(Long id, EditorialSectionRequest request) {
        EditorialSection current = findById(id);
        String name = request.name() == null ? current.getName() : request.name().trim();
        boolean available = request.publicationAvailable() == null
                ? current.isPublicationAvailable() : request.publicationAvailable();
        if (request.name() != null) {
            requireName(request.name());
            if (!current.getName().equalsIgnoreCase(name) && repository.existsByNameIgnoreCase(name)) {
                throw new ResourceConflictException("editorial section name is already in use");
            }
        }
        if (!available && current.isPublicationAvailable()
                && articleRepository.findBySection_Id(id).stream()
                .anyMatch(article -> article.getStatus() == ArticleStatus.PUBLISHED)) {
            throw new ResourceConflictException("A section with published articles cannot be disabled for publication");
        }
        current.setName(name);
        current.setPublicationAvailable(available);
        return current;
    }

    @Transactional
    public void deleteById(Long id) {
        EditorialSection section = findById(id);
        List<Article> sectionArticles = articleRepository.findBySection_Id(id);
        if (sectionArticles.stream().anyMatch(article -> article.getStatus() == ArticleStatus.PUBLISHED)) {
            throw new ResourceConflictException("A section with published articles cannot be deleted");
        }
        sectionArticles.stream().map(article -> article.getId()).forEach(articleService::deleteById);
        repository.delete(section);
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
