package org.example.lab3.services;

import org.example.lab3.dto.SectionPatchRequest;
import org.example.lab3.dto.SectionRequest;
import org.example.lab3.models.EditorialSection;
import org.example.lab3.repositories.ArticleRepository;
import org.example.lab3.repositories.EditorialSectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EditorialSectionService {
    private final EditorialSectionRepository sectionRepository;
    private final ArticleRepository articleRepository;

    public EditorialSectionService(
            EditorialSectionRepository sectionRepository,
            ArticleRepository articleRepository) {
        this.sectionRepository = sectionRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public List<EditorialSection> findAll() {
        return sectionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EditorialSection findById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Рубрика с ID " + id + " не найдена"));
    }

    @Transactional
    public EditorialSection create(SectionRequest request) {
        EditorialSection section = new EditorialSection();
        section.setName(normalize(request.name()));
        section.setAvailableForPublication(request.availableForPublication());
        return sectionRepository.save(section);
    }

    @Transactional
    public EditorialSection replace(Long id, SectionRequest request) {
        EditorialSection section = findById(id);
        section.setName(normalize(request.name()));
        section.setAvailableForPublication(request.availableForPublication());
        return section;
    }

    @Transactional
    public EditorialSection patch(Long id, SectionPatchRequest request) {
        EditorialSection section = findById(id);
        if (request.name() != null) {
            section.setName(normalize(request.name()));
        }
        if (request.availableForPublication() != null) {
            section.setAvailableForPublication(request.availableForPublication());
        }
        return section;
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        if (articleRepository.existsBySectionId(id)) {
            throw new IllegalStateException("Нельзя удалить рубрику, пока в ней есть статьи");
        }
        sectionRepository.deleteById(id);
    }

    private String normalize(String value) {
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Название рубрики не должно состоять только из пробелов");
        }
        return normalized;
    }
}
