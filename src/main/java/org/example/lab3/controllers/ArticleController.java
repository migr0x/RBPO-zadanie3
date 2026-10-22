package org.example.lab3.controllers;

import jakarta.validation.Valid;
import org.example.lab3.config.DtoMapper;
import org.example.lab3.dto.ArticlePatchRequest;
import org.example.lab3.dto.ArticleRequest;
import org.example.lab3.dto.ArticleResponse;
import org.example.lab3.services.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService service;
    private final DtoMapper mapper;

    public ArticleController(ArticleService service, DtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ArticleResponse> findAll() {
        return service.findAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ArticleResponse findById(@PathVariable Long id) {
        return mapper.toResponse(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> create(@Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(service.create(request)));
    }

    @PutMapping("/{id}")
    public ArticleResponse replace(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return mapper.toResponse(service.replace(id, request));
    }

    @PatchMapping("/{id}")
    public ArticleResponse patch(@PathVariable Long id, @Valid @RequestBody ArticlePatchRequest request) {
        return mapper.toResponse(service.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
