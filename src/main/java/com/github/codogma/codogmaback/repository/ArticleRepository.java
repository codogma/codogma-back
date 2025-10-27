package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleModel, Long>,
    JpaSpecificationExecutor<ArticleModel> {

  List<ArticleModel> findAllByUserAndStatus(UserModel user, Status status);

  @Modifying
  @Query("UPDATE ArticleModel a SET a.commentsCount = a.commentsCount + 1 WHERE a.id = :articleId")
  void incrementCommentsCount(@Param("articleId") Long articleId);

  @Modifying
  @Query("UPDATE ArticleModel a SET a.commentsCount = a.commentsCount - 1 WHERE a.id = :articleId")
  void decrementCommentsCount(@Param("articleId") Long articleId);

  @Modifying
  @Query("UPDATE ArticleModel a SET a.likesCount = a.likesCount + 1 WHERE a.id = :articleId")
  void incrementLikesCount(@Param("articleId") Long articleId);

  @Modifying
  @Query("UPDATE ArticleModel a SET a.likesCount = a.likesCount - 1 WHERE a.id = :articleId")
  void decrementLikesCount(@Param("articleId") Long articleId);

  @Modifying
  @Query("UPDATE ArticleModel a SET a.viewsCount = a.viewsCount + 1 WHERE a.id = :articleId")
  void incrementViewsCount(@Param("articleId") Long articleId);
}