package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.CommentModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentModel, Long>,
    JpaSpecificationExecutor<CommentModel> {

  List<CommentModel> findByArticleIdAndParentCommentIsNullOrderByCreatedAtAsc(Long articleId);
}