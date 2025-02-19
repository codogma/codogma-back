package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.CompilationModel;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CompilationRepository extends JpaRepository<CompilationModel, Long>,
    JpaSpecificationExecutor<CompilationModel> {

  List<CompilationModel> findTop10ByTitleStartingWithIgnoreCaseAndUser(String name, UserModel user);

  List<CompilationModel> findAllByIdInAndUser(Collection<Long> id, UserModel user);

  List<CompilationModel> findAllByUser(UserModel user);
}
