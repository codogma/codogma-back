package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.CategoryImageModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryImageRepository extends JpaRepository<CategoryImageModel, Long>,
    JpaSpecificationExecutor<CategoryImageModel> {

  Optional<CategoryImageModel> findByCategoryIdAndIsFullIsTrue(Long categoryId);

  Optional<CategoryImageModel> findByCategoryIdAndIsIconIsTrue(Long categoryId);
}
