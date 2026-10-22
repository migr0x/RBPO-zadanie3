INSERT INTO editorial_sections (id, name, publication_available) VALUES
    (1, 'Новости', TRUE),
    (2, 'Технологии', TRUE),
    (3, 'Внутренняя редактура', FALSE);

INSERT INTO app_users (id, username, display_name, email) VALUES
    (1, 'chief_editor', 'Главный редактор', 'chief@example.test'),
    (2, 'journalist_ivan', 'Иван Журналист', 'ivan@example.test'),
    (3, 'journalist_anna', 'Анна Журналист', 'anna@example.test');

INSERT INTO articles (id, section_id, author_id, title, article_text, status) VALUES
    (1, 2, 2, 'Черновик о цифровых технологиях', 'Тестовый текст статьи о развитии цифровых технологий.', 'DRAFT'),
    (2, 1, 2, 'Городская инфраструктура: обзор', 'Тестовый материал для проверки редакционного процесса.', 'REJECTED'),
    (3, 2, 3, 'Новые подходы к защите данных', 'Одобренная статья для включения в следующий выпуск.', 'APPROVED'),
    (4, 1, 3, 'Итоги спортивного сезона', 'Опубликованный материал для проверки связей выпуска.', 'PUBLISHED');

INSERT INTO editorial_reviews (id, article_id, decision, remarks, reviewed_at) VALUES
    (1, 2, 'REJECTED', 'Не хватает подтверждённых источников.', CURRENT_TIMESTAMP),
    (2, 3, 'APPROVED', 'Материал проверен и готов к публикации.', CURRENT_TIMESTAMP),
    (3, 4, 'APPROVED', 'Материал прошёл редакционную проверку.', CURRENT_TIMESTAMP);

INSERT INTO edition_issues (id, issue_date, status) VALUES
    (1, DATE '2026-11-01', 'DRAFT'),
    (2, DATE '2026-10-30', 'PUBLISHED');

INSERT INTO edition_issue_articles (issue_id, article_id, article_position) VALUES
    (1, 3, 0),
    (2, 4, 0);

ALTER TABLE editorial_sections ALTER COLUMN id RESTART WITH 10;
ALTER TABLE app_users ALTER COLUMN id RESTART WITH 10;
ALTER TABLE articles ALTER COLUMN id RESTART WITH 10;
ALTER TABLE editorial_reviews ALTER COLUMN id RESTART WITH 10;
ALTER TABLE edition_issues ALTER COLUMN id RESTART WITH 10;
