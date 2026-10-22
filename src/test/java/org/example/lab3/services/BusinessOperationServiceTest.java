package org.example.lab3.services;

import org.example.lab3.dto.ArticleRequest;
import org.example.lab3.dto.IssueRequest;
import org.example.lab3.dto.ReviewArticleRequest;
import org.example.lab3.dto.SectionRequest;
import org.example.lab3.dto.UserRequest;
import org.example.lab3.models.Article;
import org.example.lab3.models.ArticleStatus;
import org.example.lab3.models.EditionIssue;
import org.example.lab3.models.IssueStatus;
import org.example.lab3.models.ReviewDecision;
import org.example.lab3.models.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BusinessOperationServiceTest {
    @Autowired
    private EditorialUserService userService;

    @Autowired
    private EditorialSectionService sectionService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private EditionIssueService issueService;

    @Autowired
    private BusinessOperationService businessService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesUserCrudThroughRestApi() throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {"name":"REST автор","role":"AUTHOR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("REST автор"))
                .andReturn().getResponse().getContentAsString();

        Long userId = Long.valueOf(response.replaceAll(".*\\\"id\\\":(\\d+).*", "$1"));
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("AUTHOR"));
    }

    @Test
    void approvesSchedulesAndPublishesArticleInOneBusinessFlow() {
        Long authorId = userService.create(new UserRequest("Тестовый автор", UserRole.AUTHOR)).getId();
        Long sectionId = sectionService.create(new SectionRequest("Тестовая рубрика", true)).getId();
        Article article = articleService.create(new ArticleRequest(
                sectionId, authorId, "Тестовая статья", "Полный текст тестовой статьи."));

        assertThat(businessService.submitArticle(article.getId()).getStatus())
                .isEqualTo(ArticleStatus.SUBMITTED);
        assertThat(businessService.reviewArticle(article.getId(),
                new ReviewArticleRequest(ReviewDecision.APPROVED, "Можно публиковать")).getDecision())
                .isEqualTo(ReviewDecision.APPROVED);

        EditionIssue issue = issueService.create(new IssueRequest(LocalDate.of(2030, 1, 1), Set.of()));
        assertThat(businessService.addArticleToIssue(issue.getId(), article.getId()).getArticles())
                .extracting(Article::getId).containsExactly(article.getId());

        EditionIssue published = businessService.publishIssue(issue.getId());
        assertThat(published.getStatus()).isEqualTo(IssueStatus.PUBLISHED);
        assertThat(articleService.findById(article.getId()).getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
    }

    @Test
    void cancelFreesArticlesFromPlannedIssue() {
        Long authorId = userService.create(new UserRequest("Автор отмены", UserRole.AUTHOR)).getId();
        Long sectionId = sectionService.create(new SectionRequest("Рубрика отмены", true)).getId();
        Article article = articleService.create(new ArticleRequest(
                sectionId, authorId, "Статья для отмены", "Текст статьи для отмены."));
        businessService.submitArticle(article.getId());
        businessService.reviewArticle(article.getId(), new ReviewArticleRequest(
                ReviewDecision.APPROVED, "Одобрено для отменяемого выпуска"));

        EditionIssue issue = issueService.create(new IssueRequest(LocalDate.of(2030, 2, 1), Set.of(article.getId())));
        EditionIssue cancelled = businessService.cancelIssue(issue.getId());

        assertThat(cancelled.getStatus()).isEqualTo(IssueStatus.CANCELLED);
        assertThat(cancelled.getArticles()).isEmpty();
        assertThat(articleService.findById(article.getId()).getStatus()).isEqualTo(ArticleStatus.APPROVED);
    }
}
