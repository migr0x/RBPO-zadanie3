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

import java.time.Instant;

@Entity
@Table(name = "editorial_reviews")
public class EditorialReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Article article;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ReviewDecision decision;

    @Column(length = 4000)
    private String remarks;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    protected EditorialReview() {
    }

    public EditorialReview(Article article, ReviewDecision decision, String remarks, Instant reviewedAt) {
        this.article = article;
        this.decision = decision;
        this.remarks = remarks;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() {
        return id;
    }

    @JsonProperty("articleId")
    public Long getArticleId() {
        return article == null ? null : article.getId();
    }

    @JsonIgnore
    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public ReviewDecision getDecision() {
        return decision;
    }

    public void setDecision(ReviewDecision decision) {
        this.decision = decision;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
