package ru.mtusi.rbpo.lab3.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mtusi.rbpo.lab3.model.EditionIssue;
import ru.mtusi.rbpo.lab3.model.EditionStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface EditionIssueRepository extends JpaRepository<EditionIssue, Long> {

    @Override
    @EntityGraph(attributePaths = "articles")
    List<EditionIssue> findAll();

    @Override
    @EntityGraph(attributePaths = "articles")
    Optional<EditionIssue> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from EditionIssue i where i.id = :id")
    Optional<EditionIssue> findByIdForUpdate(@Param("id") Long id);

    @Query("select case when count(i) > 0 then true else false end from EditionIssue i join i.articles a where a.id = :articleId and i.status = :status")
    boolean existsPublishedContainingArticle(@Param("articleId") Long articleId,
                                             @Param("status") EditionStatus status);
}
