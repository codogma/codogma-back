package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.ConfirmationTokenModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationTokenModel, Long> {

  Optional<ConfirmationTokenModel> findByToken(String token);
}