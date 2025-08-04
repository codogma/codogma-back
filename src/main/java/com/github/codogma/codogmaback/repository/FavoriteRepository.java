package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.FavoriteModel;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<FavoriteModel, Long> {

  void deleteByUserAndCategory(UserModel user, CategoryModel categoryModel);

  boolean existsByUserAndCategory(UserModel user, CategoryModel categoryModel);

  List<FavoriteModel> findByUser(UserModel user);
}