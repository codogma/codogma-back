package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.TagModel;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<TagModel, Long> {

  List<TagModel> findTop10ByNameStartingWithIgnoreCase(String name);

  @Query(value = "SELECT t.* FROM tags t " +
      "JOIN article_tags at ON t.id = at.tag_id " +
      "JOIN article_categories ac ON at.article_id = ac.article_id " +
      "WHERE ac.category_id = :categoryId " +
      "GROUP BY t.id " +
      "ORDER BY COUNT(at.article_id) DESC " +
      "LIMIT 10",
      nativeQuery = true)
  List<TagModel> findTop10TagsByCategoryId(@Param("categoryId") Long categoryId);

  List<TagModel> findAllByNameIgnoreCaseIn(Collection<String> name);
}