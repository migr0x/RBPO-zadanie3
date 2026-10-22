package org.example.lab3;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Запускает настоящий PostgreSQL-бинарник, а не H2 и не mock.
 * Сервер и временный каталог удаляются после завершения тестового набора.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostgreSqlStartupIntegrationTest {
    private static final String APP_USER = "editorial_app";
    private static final String APP_PASSWORD = "test" + UUID.randomUUID().toString().replace("-", "");
    private static final EmbeddedPostgres POSTGRES = startPostgres();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DataSource dataSource;

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl(APP_USER, "postgres"));
        registry.add("spring.datasource.username", () -> APP_USER);
        registry.add("spring.datasource.password", () -> APP_PASSWORD);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.sql.init.mode", () -> "never");
    }

    @Test
    void startsSpringBootAgainstRestrictedPostgreSqlUserAndServesRestApi() throws Exception {
        var created = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users",
                new CreateUserRequest("Проверка PostgreSQL", "AUTHOR"),
                String.class);

        assertThat(created.getStatusCode().value()).isEqualTo(201);
        assertThat(created.getBody()).contains("Проверка PostgreSQL");

        assertThatThrownBy(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE forbidden_for_application (id bigint)");
            }
        }).isInstanceOf(SQLException.class);
    }

    @AfterAll
    static void stopPostgres() throws Exception {
        POSTGRES.close();
    }

    private static EmbeddedPostgres startPostgres() {
        try {
            EmbeddedPostgres postgres = EmbeddedPostgres.builder().setPort(0).start();
            try (Connection connection = postgres.getPostgresDatabase().getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE ROLE " + APP_USER
                        + " LOGIN PASSWORD '" + APP_PASSWORD
                        + "' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT");
                createSchema(statement);
            }
            return postgres;
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось запустить временный PostgreSQL", exception);
        }
    }

    private static void createSchema(Statement statement) throws SQLException {
        statement.execute("REVOKE ALL ON SCHEMA public FROM PUBLIC");
        statement.execute("GRANT USAGE ON SCHEMA public TO " + APP_USER);
        statement.execute("""
                CREATE TABLE editorial_users (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL UNIQUE,
                    role VARCHAR(30) NOT NULL,
                    CONSTRAINT chk_editorial_users_role CHECK (role IN ('AUTHOR', 'EDITOR', 'CHIEF_EDITOR'))
                )
                """);
        statement.execute("""
                CREATE TABLE editorial_sections (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL UNIQUE,
                    available_for_publication BOOLEAN NOT NULL
                )
                """);
        statement.execute("""
                CREATE TABLE articles (
                    id BIGSERIAL PRIMARY KEY,
                    section_id BIGINT NOT NULL REFERENCES editorial_sections(id) ON DELETE RESTRICT,
                    author_id BIGINT NOT NULL REFERENCES editorial_users(id) ON DELETE RESTRICT,
                    title VARCHAR(200) NOT NULL,
                    text TEXT NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    CONSTRAINT chk_articles_title CHECK (char_length(btrim(title)) > 0),
                    CONSTRAINT chk_articles_text CHECK (char_length(btrim(text)) > 0),
                    CONSTRAINT chk_articles_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'PUBLISHED'))
                )
                """);
        statement.execute("""
                CREATE TABLE editorial_reviews (
                    id BIGSERIAL PRIMARY KEY,
                    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE RESTRICT,
                    decision VARCHAR(20) NOT NULL,
                    notes VARCHAR(1000) NOT NULL,
                    reviewed_at TIMESTAMP NOT NULL,
                    CONSTRAINT chk_editorial_reviews_decision CHECK (decision IN ('APPROVED', 'REJECTED')),
                    CONSTRAINT chk_editorial_reviews_notes CHECK (char_length(btrim(notes)) > 0)
                )
                """);
        statement.execute("""
                CREATE TABLE edition_issues (
                    id BIGSERIAL PRIMARY KEY,
                    issue_date DATE NOT NULL UNIQUE,
                    status VARCHAR(20) NOT NULL,
                    CONSTRAINT chk_edition_issues_status CHECK (status IN ('PLANNED', 'PUBLISHED', 'CANCELLED'))
                )
                """);
        statement.execute("""
                CREATE TABLE issue_articles (
                    issue_id BIGINT NOT NULL REFERENCES edition_issues(id) ON DELETE CASCADE,
                    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE RESTRICT,
                    PRIMARY KEY (issue_id, article_id)
                )
                """);
        statement.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO " + APP_USER);
        statement.execute("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO " + APP_USER);
    }

    private record CreateUserRequest(String name, String role) {
    }
}
