package org.example.lab3.controllers;

import jakarta.validation.Valid;
import org.example.lab3.config.DtoMapper;
import org.example.lab3.dto.ReviewPatchRequest;
import org.example.lab3.dto.ReviewRequest;
import org.example.lab3.dto.ReviewResponse;
import org.example.lab3.services.EditorialReviewService;
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
@RequestMapping("/api/reviews")
public class EditorialReviewController {
    private final EditorialReviewService service;
    private final DtoMapper mapper;

    public EditorialReviewController(EditorialReviewService service, DtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ReviewResponse> findAll() {
        return service.findAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ReviewResponse findById(@PathVariable Long id) {
        return mapper.toResponse(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(service.create(request)));
    }

    @PutMapping("/{id}")
    public ReviewResponse replace(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return mapper.toResponse(service.replace(id, request));
    }

    @PatchMapping("/{id}")
    public ReviewResponse patch(@PathVariable Long id, @Valid @RequestBody ReviewPatchRequest request) {
        return mapper.toResponse(service.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
