package ru.mtusi.rbpo.lab3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.mtusi.rbpo.lab3.repository.ArticleRepository;
import ru.mtusi.rbpo.lab3.repository.EditionIssueRepository;
import ru.mtusi.rbpo.lab3.repository.EditorialReviewRepository;
import ru.mtusi.rbpo.lab3.repository.EditorialSectionRepository;
import ru.mtusi.rbpo.lab3.repository.UserRepository;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbpoLab3IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EditionIssueRepository issueRepository;

    @Autowired
    private EditorialReviewRepository reviewRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private EditorialSectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        issueRepository.deleteAll();
        reviewRepository.deleteAll();
        articleRepository.deleteAll();
        sectionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void crudApiWorksForAllFiveEntities() throws Exception {
        long sectionId = createSection("Технологии", true);
        long userId = createUser("journalist", "Иван Журналист", "ivan@example.test");

        String articleJson = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sectionId":%d,"authorId":%d,"title":"Первая статья","text":"Текст первой статьи","status":"DRAFT"}
                                """.formatted(sectionId, userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sectionId").value(sectionId))
                .andReturn().getResponse().getContentAsString();
        long articleId = objectMapper.readTree(articleJson).get("id").asLong();

        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/articles/{id}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorId").value(userId));
        mockMvc.perform(patch("/api/articles/{id}", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Обновлённая статья\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Обновлённая статья"));

        String reviewJson = mockMvc.perform(post("/api/editorial-reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"articleId":%d,"decision":"NEEDS_REVISION","remarks":"Добавить источник","reviewedAt":"2000-01-01T00:00:00Z"}
                                """.formatted(articleId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewedAt").exists())
                .andReturn().getResponse().getContentAsString();
        long reviewId = objectMapper.readTree(reviewJson).get("id").asLong();
        mockMvc.perform(get("/api/editorial-reviews")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/editorial-reviews/{id}", reviewId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.articleId").value(articleId));
        mockMvc.perform(patch("/api/editorial-reviews/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Источник добавлен\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.remarks").value("Источник добавлен"));
        mockMvc.perform(delete("/api/editorial-reviews/{id}", reviewId)).andExpect(status().isNoContent());

        String issueJson = mockMvc.perform(post("/api/edition-issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-11-05","articleIds":[],"status":"DRAFT"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.articleIds", hasSize(0)))
                .andReturn().getResponse().getContentAsString();
        long issueId = objectMapper.readTree(issueJson).get("id").asLong();
        mockMvc.perform(get("/api/edition-issues")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/edition-issues/{id}", issueId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.articleIds", hasSize(0)));
        mockMvc.perform(patch("/api/edition-issues/{id}", issueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-11-06\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.date").value("2026-11-06"));
        mockMvc.perform(delete("/api/edition-issues/{id}", issueId)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/editorial-sections")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/editorial-sections/{id}", sectionId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicationAvailable").value(true));
        mockMvc.perform(patch("/api/editorial-sections/{id}", sectionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Наука и технологии\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Наука и технологии"));

        mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("journalist"));
        mockMvc.perform(patch("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Иван Автор\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.displayName").value("Иван Автор"));

        mockMvc.perform(delete("/api/articles/{id}", articleId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/editorial-sections/{id}", sectionId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/users/{id}", userId)).andExpect(status().isNoContent());
    }

    @Test
    void editorialWorkflowKeepsArticleReviewAndIssueConsistent() throws Exception {
        long sectionId = createSection("Новости", true);
        long userId = createUser("reporter", "Корреспондент", "reporter@example.test");
        long articleId = createArticle(sectionId, userId, "Готовим материал");
        long issueId = createIssue("2026-11-07");

        mockMvc.perform(post("/api/business-operations/articles/{id}/submit-for-review", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_REVIEW"));

        mockMvc.perform(post("/api/business-operations/articles/{id}/approve", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Проверено редактором\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.status").value("APPROVED"))
                .andExpect(jsonPath("$.review.decision").value("APPROVED"));

        long secondaryIssueId = createIssueWithArticle("2026-11-08", articleId);

        mockMvc.perform(post("/api/business-operations/issues/{issueId}/articles/{articleId}", issueId, articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleIds", contains((int) articleId)));

        mockMvc.perform(patch("/api/edition-issues/{id}", issueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-11-10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleIds", contains((int) articleId)));

        mockMvc.perform(post("/api/business-operations/issues/{id}/publish", issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.articleIds", contains((int) articleId)));
        mockMvc.perform(get("/api/articles/{id}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
        mockMvc.perform(get("/api/edition-issues/{id}", secondaryIssueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleIds", hasSize(0)));
        mockMvc.perform(get("/api/editorial-reviews")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void requestRevisionCreatesReviewAndReturnsArticleToDraft() throws Exception {
        long sectionId = createSection("Общество", true);
        long userId = createUser("writer", "Автор", "writer@example.test");
        long articleId = createArticle(sectionId, userId, "Материал на доработку");

        mockMvc.perform(post("/api/business-operations/articles/{id}/submit-for-review", articleId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/business-operations/articles/{id}/request-revision", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Нужны дополнительные источники\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.status").value("DRAFT"))
                .andExpect(jsonPath("$.review.decision").value("NEEDS_REVISION"));
        mockMvc.perform(get("/api/editorial-reviews")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void invalidTransitionsAreRejected() throws Exception {
        long sectionId = createSection("Наука", true);
        long userId = createUser("scientist", "Автор науки", "science@example.test");
        long articleId = createArticle(sectionId, userId, "Новый материал");
        long issueId = createIssue("2026-11-08");

        mockMvc.perform(post("/api/business-operations/issues/{issueId}/articles/{articleId}", issueId, articleId))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/edition-issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-11-08\",\"articleIds\":[%d],\"status\":\"DRAFT\"}".formatted(articleId)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/business-operations/issues/{id}/publish", issueId))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/business-operations/articles/{id}/approve", articleId)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void deletingSectionCascadesToArticlesReviewsAndIssueLinks() throws Exception {
        long sectionId = createSection("Редакционная политика", true);
        long userId = createUser("policy_writer", "Автор рубрики", "policy@example.test");
        long articleId = createArticle(sectionId, userId, "Материал рубрики");
        approveArticle(articleId);
        long issueId = createIssueWithArticle("2026-11-09", articleId);

        mockMvc.perform(delete("/api/editorial-sections/{id}", sectionId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/articles/{id}", articleId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/editorial-reviews")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/edition-issues/{id}", issueId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.articleIds", hasSize(0)));
    }

    @Test
    void deletingUserCascadesToArticlesReviewsAndIssueLinks() throws Exception {
        long sectionId = createSection("Экономика", true);
        long userId = createUser("economist", "Экономический обозреватель", "economist@example.test");
        long articleId = createArticle(sectionId, userId, "Экономическая заметка");
        approveArticle(articleId);
        long issueId = createIssueWithArticle("2026-11-09", articleId);

        mockMvc.perform(delete("/api/users/{id}", userId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/articles/{id}", articleId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/editorial-reviews")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/edition-issues/{id}", issueId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.articleIds", hasSize(0)));
    }

    @Test
    void editorialReviewHistoryCannotContradictArticleState() throws Exception {
        long sectionId = createSection("Редакторская проверка", true);
        long userId = createUser("review_writer", "Автор материала", "review@example.test");
        long articleId = createArticle(sectionId, userId, "Материал для редактора");

        mockMvc.perform(post("/api/editorial-reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"articleId\":%d,\"decision\":\"APPROVED\"}".formatted(articleId)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/business-operations/articles/{id}/submit-for-review", articleId))
                .andExpect(status().isOk());
        String approvalJson = mockMvc.perform(post("/api/business-operations/articles/{id}/approve", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Проверено\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long reviewId = objectMapper.readTree(approvalJson).get("review").get("id").asLong();

        mockMvc.perform(patch("/api/editorial-reviews/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"NEEDS_REVISION\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/editorial-reviews/{id}", reviewId))
                .andExpect(status().isConflict());
        mockMvc.perform(patch("/api/editorial-reviews/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Уточнены редакторские замечания\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("APPROVED"));
    }

    @Test
    void publishedArticlesAndIssuesCannotBeDeleted() throws Exception {
        long sectionId = createSection("Публикации", true);
        long userId = createUser("published_writer", "Автор публикаций", "published@example.test");
        long articleId = createArticle(sectionId, userId, "Опубликованный материал");
        long issueId = createIssue("2026-11-10");

        approveArticle(articleId);
        mockMvc.perform(post("/api/business-operations/issues/{issueId}/articles/{articleId}", issueId, articleId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/business-operations/issues/{id}/publish", issueId))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/articles/{id}", articleId)).andExpect(status().isConflict());
        mockMvc.perform(delete("/api/edition-issues/{id}", issueId)).andExpect(status().isConflict());
        mockMvc.perform(delete("/api/users/{id}", userId)).andExpect(status().isConflict());
        mockMvc.perform(delete("/api/editorial-sections/{id}", sectionId)).andExpect(status().isConflict());

        mockMvc.perform(get("/api/articles/{id}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
        mockMvc.perform(get("/api/edition-issues/{id}", issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    private void approveArticle(long articleId) throws Exception {
        mockMvc.perform(post("/api/business-operations/articles/{id}/submit-for-review", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_REVIEW"));
        mockMvc.perform(post("/api/business-operations/articles/{id}/approve", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Проверено для теста каскадного удаления\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.status").value("APPROVED"));
    }

    private long createSection(String name, boolean publicationAvailable) throws Exception {
        String response = mockMvc.perform(post("/api/editorial-sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SectionPayload(name, publicationAvailable))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createUser(String username, String displayName, String email) throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserPayload(username, displayName, email))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createArticle(long sectionId, long userId, String title) throws Exception {
        String response = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sectionId":%d,"authorId":%d,"title":"%s","text":"Подробный тестовый текст материала","status":"DRAFT"}
                                """.formatted(sectionId, userId, title)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asLong();
    }

    private long createIssue(String date) throws Exception {
        String response = mockMvc.perform(post("/api/edition-issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"%s\",\"articleIds\":[],\"status\":\"DRAFT\"}".formatted(date)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createIssueWithArticle(String date, long articleId) throws Exception {
        String response = mockMvc.perform(post("/api/edition-issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"%s\",\"articleIds\":[%d],\"status\":\"DRAFT\"}".formatted(date, articleId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private record SectionPayload(String name, Boolean publicationAvailable) {
    }

    private record UserPayload(String username, String displayName, String email) {
    }
}
