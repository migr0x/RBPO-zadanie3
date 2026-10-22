package ru.mtusi.rbpo.lab3.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mtusi.rbpo.lab3.dto.ArticleRequest;
import ru.mtusi.rbpo.lab3.model.Article;
import ru.mtusi.rbpo.lab3.service.ArticleService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService service;

    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @GetMapping
    public List<Article> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Article findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Article> create(@RequestBody ArticleRequest request) {
        Article created = service.create(request);
        return ResponseEntity.created(URI.create("/api/articles/" + created.getId())).body(created);
    }

    @PatchMapping("/{id}")
    public Article update(@PathVariable Long id, @RequestBody ArticleRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
