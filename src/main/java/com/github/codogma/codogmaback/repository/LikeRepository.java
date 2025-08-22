package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.LikeModel;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<LikeModel, Long> {

  Boolean existsByUserAndArticle(UserModel userModel, ArticleModel articleModel);

  Optional<LikeModel> findByArticleAndUser(ArticleModel article, UserModel user);
}