package org.example.lab3.controllers;

import jakarta.validation.Valid;
import org.example.lab3.config.DtoMapper;
import org.example.lab3.dto.IssuePatchRequest;
import org.example.lab3.dto.IssueRequest;
import org.example.lab3.dto.IssueResponse;
import org.example.lab3.services.EditionIssueService;
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
@RequestMapping("/api/issues")
public class EditionIssueController {
    private final EditionIssueService service;
    private final DtoMapper mapper;

    public EditionIssueController(EditionIssueService service, DtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<IssueResponse> findAll() {
        return service.findAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public IssueResponse findById(@PathVariable Long id) {
        return mapper.toResponse(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<IssueResponse> create(@Valid @RequestBody IssueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(service.create(request)));
    }

    @PutMapping("/{id}")
    public IssueResponse replace(@PathVariable Long id, @Valid @RequestBody IssueRequest request) {
        return mapper.toResponse(service.replace(id, request));
    }

    @PatchMapping("/{id}")
    public IssueResponse patch(@PathVariable Long id, @Valid @RequestBody IssuePatchRequest request) {
        return mapper.toResponse(service.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
