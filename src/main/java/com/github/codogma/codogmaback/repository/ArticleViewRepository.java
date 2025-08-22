package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.ArticleView;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleViewRepository extends JpaRepository<ArticleView, Long>,
    JpaSpecificationExecutor<ArticleView> {

  Optional<ArticleView> findByUserAndArticle(UserModel userModel, ArticleModel article);

  List<ArticleView> findTop20ByUserOrderByUpdatedAtDesc(UserModel userModel);
}