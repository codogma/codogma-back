package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.ArticleImageModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleImageRepository extends JpaRepository<ArticleImageModel, Long>,
    JpaSpecificationExecutor<ArticleImageModel> {

  Optional<ArticleImageModel> findByArticleIdAndIsPreviewIsTrue(Long articleId);
}