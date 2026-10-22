package ru.mtusi.rbpo.lab3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EditorialSection section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User author;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(name = "article_text", nullable = false, length = 20000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ArticleStatus status;

    protected Article() {
    }

    public Article(EditorialSection section, User author, String title, String text, ArticleStatus status) {
        this.section = section;
        this.author = author;
        this.title = title;
        this.text = text;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    @JsonProperty("sectionId")
    public Long getSectionId() {
        return section == null ? null : section.getId();
    }

    @JsonIgnore
    public EditorialSection getSection() {
        return section;
    }

    public void setSection(EditorialSection section) {
        this.section = section;
    }

    @JsonProperty("authorId")
    public Long getAuthorId() {
        return author == null ? null : author.getId();
    }

    @JsonIgnore
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArticleStatus getStatus() {
        return status;
    }

    public void setStatus(ArticleStatus status) {
        this.status = status;
    }
}
