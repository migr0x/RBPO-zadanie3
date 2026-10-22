package ru.mtusi.rbpo.lab3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtusi.rbpo.lab3.model.EditorialReview;

import java.util.List;

@Repository
public interface EditorialReviewRepository extends JpaRepository<EditorialReview, Long> {

    List<EditorialReview> findByArticle_Id(Long articleId);

    void deleteAllByArticle_Id(Long articleId);
}
