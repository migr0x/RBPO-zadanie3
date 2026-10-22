package org.example.lab3.repositories;

import org.example.lab3.models.EditionIssue;
import org.example.lab3.models.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface EditionIssueRepository extends JpaRepository<EditionIssue, Long> {
    boolean existsByArticlesIdAndStatusIn(Long articleId, Collection<IssueStatus> statuses);

    boolean existsByArticlesId(Long articleId);
}
