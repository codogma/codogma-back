package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.RefreshTokenModel;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, UUID> {

  Optional<RefreshTokenModel> findByJti(String jti);

  List<RefreshTokenModel> findAllByUserUsernameAndDeviceId(String username, String deviceId);

  boolean existsByTokenHash(String tokenHash);

  void deleteByUserUsernameAndDeviceId(String username, String deviceId);

  void deleteByUserUsername(String username);

  void deleteByExpiresAtBefore(Instant expiredAt);

  int countByUserId(Long userId);

  List<RefreshTokenModel> findByUserIdOrderByCreatedAtDesc(Long userId);
}