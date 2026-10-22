package ru.mtusi.rbpo.lab3.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtusi.rbpo.lab3.dto.EditorialDecisionResult;
import ru.mtusi.rbpo.lab3.dto.WorkflowCommandRequest;
import ru.mtusi.rbpo.lab3.model.Article;
import ru.mtusi.rbpo.lab3.model.EditionIssue;
import ru.mtusi.rbpo.lab3.service.EditorialWorkflowService;

@RestController
@RequestMapping("/api/business-operations")
public class BusinessOperationController {

    private final EditorialWorkflowService workflowService;

    public BusinessOperationController(EditorialWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/articles/{articleId}/submit-for-review")
    public Article submitForReview(@PathVariable Long articleId) {
        return workflowService.submitForReview(articleId);
    }

    @PostMapping("/articles/{articleId}/approve")
    public EditorialDecisionResult approve(@PathVariable Long articleId,
                                           @RequestBody(required = false) WorkflowCommandRequest request) {
        return workflowService.approveArticle(articleId, request == null ? null : request.remarks());
    }

    @PostMapping("/articles/{articleId}/request-revision")
    public EditorialDecisionResult requestRevision(@PathVariable Long articleId,
                                                   @RequestBody(required = false) WorkflowCommandRequest request) {
        return workflowService.requestRevision(articleId, request == null ? null : request.remarks());
    }

    @PostMapping("/issues/{issueId}/articles/{articleId}")
    public EditionIssue includeArticle(@PathVariable Long issueId, @PathVariable Long articleId) {
        return workflowService.includeArticle(issueId, articleId);
    }

    @PostMapping("/issues/{issueId}/publish")
    public EditionIssue publishIssue(@PathVariable Long issueId) {
        return workflowService.publishIssue(issueId);
    }
}
