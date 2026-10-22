package org.example.lab3.controllers;

import jakarta.validation.Valid;
import org.example.lab3.config.DtoMapper;
import org.example.lab3.dto.UserPatchRequest;
import org.example.lab3.dto.UserRequest;
import org.example.lab3.dto.UserResponse;
import org.example.lab3.services.EditorialUserService;
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
@RequestMapping("/api/users")
public class EditorialUserController {
    private final EditorialUserService service;
    private final DtoMapper mapper;

    public EditorialUserController(EditorialUserService service, DtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return service.findAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return mapper.toResponse(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(service.create(request)));
    }

    @PutMapping("/{id}")
    public UserResponse replace(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return mapper.toResponse(service.replace(id, request));
    }

    @PatchMapping("/{id}")
    public UserResponse patch(@PathVariable Long id, @Valid @RequestBody UserPatchRequest request) {
        return mapper.toResponse(service.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
