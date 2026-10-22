package org.example.lab3.controllers;

import jakarta.validation.Valid;
import org.example.lab3.config.DtoMapper;
import org.example.lab3.dto.SectionPatchRequest;
import org.example.lab3.dto.SectionRequest;
import org.example.lab3.dto.SectionResponse;
import org.example.lab3.services.EditorialSectionService;
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
@RequestMapping("/api/sections")
public class EditorialSectionController {
    private final EditorialSectionService service;
    private final DtoMapper mapper;

    public EditorialSectionController(EditorialSectionService service, DtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<SectionResponse> findAll() {
        return service.findAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public SectionResponse findById(@PathVariable Long id) {
        return mapper.toResponse(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<SectionResponse> create(@Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(service.create(request)));
    }

    @PutMapping("/{id}")
    public SectionResponse replace(@PathVariable Long id, @Valid @RequestBody SectionRequest request) {
        return mapper.toResponse(service.replace(id, request));
    }

    @PatchMapping("/{id}")
    public SectionResponse patch(@PathVariable Long id, @Valid @RequestBody SectionPatchRequest request) {
        return mapper.toResponse(service.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
