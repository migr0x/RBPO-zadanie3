package ru.mtusi.rbpo.lab3.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mtusi.rbpo.lab3.dto.EditorialReviewRequest;
import ru.mtusi.rbpo.lab3.model.EditorialReview;
import ru.mtusi.rbpo.lab3.service.EditorialReviewService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/editorial-reviews")
public class EditorialReviewController {

    private final EditorialReviewService service;

    public EditorialReviewController(EditorialReviewService service) {
        this.service = service;
    }

    @GetMapping
    public List<EditorialReview> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public EditorialReview findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<EditorialReview> create(@RequestBody EditorialReviewRequest request) {
        EditorialReview created = service.create(request);
        return ResponseEntity.created(URI.create("/api/editorial-reviews/" + created.getId())).body(created);
    }

    @PatchMapping("/{id}")
    public EditorialReview update(@PathVariable Long id, @RequestBody EditorialReviewRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
