package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.CompilationArticleModel;
import com.github.codogma.codogmaback.model.CompilationModel;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompilationArticleRepository extends JpaRepository<CompilationArticleModel, Long> {

  List<CompilationArticleModel> findByArticleAndCompilationUser(ArticleModel article,
      UserModel userModel);

  List<CompilationArticleModel> findAllByArticleAndCompilationUser(ArticleModel article,
      UserModel userModel);

  List<CompilationArticleModel> findByCompilationOrderByPosition(CompilationModel compilation);
}