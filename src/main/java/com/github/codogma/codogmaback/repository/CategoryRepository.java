package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.CategoryModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, Long>,
    JpaSpecificationExecutor<CategoryModel> {

  @Query(value = "SELECT c.* FROM categories c "
      + "JOIN article_categories ac ON c.id = ac.category_id "
      + "JOIN articles a ON ac.article_id = a.id " + "WHERE a.user_id = :userId "
      + "GROUP BY c.id", nativeQuery = true)
  List<CategoryModel> findCategoriesByUserId(@Param("userId") Long userId);

  @Query(value = """
      SELECT c.* FROM categories c
      JOIN category_localized_names cln ON c.id = cln.category_id
      WHERE cln.language = :language
        AND LOWER(cln.name) LIKE LOWER(CONCAT(:name, '%'))
      LIMIT 10
      """, nativeQuery = true)
  List<CategoryModel> findTop10ByNameStartingWithIgnoreCase(@Param("language") String language,
      @Param("name") String name);

  @Query("SELECT n FROM CategoryModel n " +
      "LEFT JOIN FETCH n.name " +
      "LEFT JOIN FETCH n.description " +
      "WHERE n.id = :id")
  Optional<CategoryModel> findByIdWithCollections(@Param("id") Long userId);
}
