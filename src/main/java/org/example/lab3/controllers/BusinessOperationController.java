package org.example.lab3.controllers;

import jakarta.validation.Valid;
import org.example.lab3.config.DtoMapper;
import org.example.lab3.dto.ArticleResponse;
import org.example.lab3.dto.IssueResponse;
import org.example.lab3.dto.ReviewArticleRequest;
import org.example.lab3.dto.ReviewResponse;
import org.example.lab3.services.BusinessOperationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business")
public class BusinessOperationController {
    private final BusinessOperationService service;
    private final DtoMapper mapper;

    public BusinessOperationController(BusinessOperationService service, DtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping("/articles/{articleId}/submit")
    public ArticleResponse submitArticle(@PathVariable Long articleId) {
        return mapper.toResponse(service.submitArticle(articleId));
    }

    @PostMapping("/articles/{articleId}/review")
    public ResponseEntity<ReviewResponse> reviewArticle(
            @PathVariable Long articleId,
            @Valid @RequestBody ReviewArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(service.reviewArticle(articleId, request)));
    }

    @PostMapping("/issues/{issueId}/articles/{articleId}")
    public IssueResponse addArticleToIssue(
            @PathVariable Long issueId,
            @PathVariable Long articleId) {
        return mapper.toResponse(service.addArticleToIssue(issueId, articleId));
    }

    @PostMapping("/issues/{issueId}/publish")
    public IssueResponse publishIssue(@PathVariable Long issueId) {
        return mapper.toResponse(service.publishIssue(issueId));
    }

    @PostMapping("/issues/{issueId}/cancel")
    public IssueResponse cancelIssue(@PathVariable Long issueId) {
        return mapper.toResponse(service.cancelIssue(issueId));
    }
}
