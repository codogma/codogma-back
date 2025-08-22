package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.BookmarkModel;
import com.github.codogma.codogmaback.model.CompilationModel;
import com.github.codogma.codogmaback.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<BookmarkModel, Long> {

  void deleteByUserAndCompilation(UserModel user, CompilationModel article);

  boolean existsByUserAndCompilation(UserModel user, CompilationModel article);
}