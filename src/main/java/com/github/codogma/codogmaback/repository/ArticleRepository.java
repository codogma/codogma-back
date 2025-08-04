package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleModel, Long>,
    JpaSpecificationExecutor<ArticleModel> {

  List<ArticleModel> findAllByUserAndStatus(UserModel user, Status status);
}