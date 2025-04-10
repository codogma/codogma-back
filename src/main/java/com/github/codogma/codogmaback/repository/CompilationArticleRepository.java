package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.CompilationArticle;
import com.github.codogma.codogmaback.model.CompilationModel;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompilationArticleRepository extends JpaRepository<CompilationArticle, Long> {

  List<CompilationArticle> findByArticleAndCompilationUser(ArticleModel article,
      UserModel userModel);

  List<CompilationArticle> findAllByArticleAndCompilationUser(ArticleModel article,
      UserModel userModel);

  List<CompilationArticle> findByCompilationOrderByPosition(CompilationModel compilation);
}
