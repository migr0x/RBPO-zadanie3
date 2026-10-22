INSERT INTO editorial_sections (name, available_for_publication)
VALUES ('Технологии', TRUE), ('Культура', FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO editorial_users (name, role)
VALUES ('Иван Автор', 'AUTHOR'), ('Анна Редактор', 'EDITOR')
ON CONFLICT (name) DO NOTHING;

INSERT INTO articles (section_id, author_id, title, text, status)
SELECT section.id, author.id, 'Безопасная разработка ПО', 'Демонстрационная одобренная статья.', 'APPROVED'
FROM editorial_sections section
JOIN editorial_users author ON author.name = 'Иван Автор'
WHERE section.name = 'Технологии'
  AND NOT EXISTS (SELECT 1 FROM articles WHERE title = 'Безопасная разработка ПО');

INSERT INTO articles (section_id, author_id, title, text, status)
SELECT section.id, author.id, 'Черновик культурной заметки', 'Демонстрационный черновик статьи.', 'DRAFT'
FROM editorial_sections section
JOIN editorial_users author ON author.name = 'Иван Автор'
WHERE section.name = 'Культура'
  AND NOT EXISTS (SELECT 1 FROM articles WHERE title = 'Черновик культурной заметки');

INSERT INTO editorial_reviews (article_id, decision, notes, reviewed_at)
SELECT article.id, 'APPROVED', 'Материал прошёл редакторскую проверку.', CURRENT_TIMESTAMP
FROM articles article
WHERE article.title = 'Безопасная разработка ПО'
  AND NOT EXISTS (SELECT 1 FROM editorial_reviews review WHERE review.article_id = article.id);

INSERT INTO edition_issues (issue_date, status)
VALUES (DATE '2026-12-15', 'PLANNED')
ON CONFLICT (issue_date) DO NOTHING;

INSERT INTO issue_articles (issue_id, article_id)
SELECT issue.id, article.id
FROM edition_issues issue
JOIN articles article ON article.title = 'Безопасная разработка ПО'
WHERE issue.issue_date = DATE '2026-12-15'
  AND NOT EXISTS (
      SELECT 1 FROM issue_articles link
      WHERE link.issue_id = issue.id AND link.article_id = article.id
  );
