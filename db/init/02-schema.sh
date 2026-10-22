#!/usr/bin/env sh
set -eu

psql --set=ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --set=app_user="$APP_DB_USER" \
  --set=db_name="$POSTGRES_DB" <<'SQL'
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT USAGE ON SCHEMA public TO :"app_user";

CREATE TABLE editorial_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL,
    CONSTRAINT chk_editorial_users_role CHECK (role IN ('AUTHOR', 'EDITOR', 'CHIEF_EDITOR'))
);

CREATE TABLE editorial_sections (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    available_for_publication BOOLEAN NOT NULL
);

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
);

CREATE TABLE editorial_reviews (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE RESTRICT,
    decision VARCHAR(20) NOT NULL,
    notes VARCHAR(1000) NOT NULL,
    reviewed_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_editorial_reviews_decision CHECK (decision IN ('APPROVED', 'REJECTED')),
    CONSTRAINT chk_editorial_reviews_notes CHECK (char_length(btrim(notes)) > 0)
);

CREATE TABLE edition_issues (
    id BIGSERIAL PRIMARY KEY,
    issue_date DATE NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT chk_edition_issues_status CHECK (status IN ('PLANNED', 'PUBLISHED', 'CANCELLED'))
);

CREATE TABLE issue_articles (
    issue_id BIGINT NOT NULL REFERENCES edition_issues(id) ON DELETE CASCADE,
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE RESTRICT,
    PRIMARY KEY (issue_id, article_id)
);

GRANT CONNECT ON DATABASE :"db_name" TO :"app_user";
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO :"app_user";
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO :"app_user";
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO :"app_user";
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO :"app_user";
SQL
