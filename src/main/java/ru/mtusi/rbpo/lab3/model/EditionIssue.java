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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "edition_issues")
public class EditionIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_date", nullable = false)
    private LocalDate date;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "edition_issue_articles",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id"))
    @OrderColumn(name = "article_position")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private List<Article> articles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EditionStatus status;

    protected EditionIssue() {
    }

    public EditionIssue(LocalDate date, List<Article> articles, EditionStatus status) {
        this.date = date;
        setArticles(articles);
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @JsonProperty(value = "articleIds", access = JsonProperty.Access.READ_ONLY)
    public List<Long> getArticleIds() {
        return articles.stream().map(Article::getId).toList();
    }

    @JsonIgnore
    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        List<Article> replacement = articles == null ? List.of() : new ArrayList<>(articles);
        if (this.articles == null) {
            this.articles = new ArrayList<>();
        } else {
            this.articles.clear();
        }
        this.articles.addAll(replacement);
    }

    public EditionStatus getStatus() {
        return status;
    }

    public void setStatus(EditionStatus status) {
        this.status = status;
    }
}
