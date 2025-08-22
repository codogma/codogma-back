package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.ConfirmationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

  Optional<ConfirmationToken> findByToken(String token);
}