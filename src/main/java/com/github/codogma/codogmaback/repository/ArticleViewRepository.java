package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.ArticleViewModel;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleViewRepository extends JpaRepository<ArticleViewModel, Long>,
    JpaSpecificationExecutor<ArticleViewModel> {

  Optional<ArticleViewModel> findByUserAndArticle(UserModel userModel, ArticleModel article);

  List<ArticleViewModel> findTop20ByUserOrderByUpdatedAtDesc(UserModel userModel);
}