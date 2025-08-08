package com.github.codogma.codogmaback.security;

import com.github.codogma.codogmaback.exception.AccessTokenExpiredException;
import com.github.codogma.codogmaback.exception.RefreshTokenExpiredException;
import com.github.codogma.codogmaback.model.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  @Value("${spring.security.jwt.secret-key}")
  private String secretKey;
  @Value("${spring.security.jwt.device-claim-name}")
  private String deviceClaimName;
  @Value("${spring.security.jwt.issuer}")
  private String issuer;
  @Value("${spring.security.jwt.alg}")
  private String alg;
  @Value("${spring.security.jwt.kid}")
  private String kid;
  @Value("${spring.security.jwt.audience}")
  private String audience;
  @Value("${spring.security.jwt.access-expiration}")
  private long accessExpiration;
  @Value("${spring.security.jwt.refresh-expiration}")
  private long refreshExpiration;

  private final SecureRandom secureRandom = new SecureRandom();

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateAccessToken(UserModel userModel, String deviceId) {
    Instant now = Instant.now();
    return Jwts.builder().header().type("JWT").add("alg", alg).add("kid", kid).and()
        .subject(userModel.getUsername()).issuer(issuer).audience().add(audience).and()
        .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(accessExpiration)))
        .id(UUID.randomUUID().toString()).claim("role", userModel.getRole().name())
        .claim(deviceClaimName, deviceId).claim("rnd", secureRandom.nextInt())
        .signWith(getSignInKey(), Jwts.SIG.HS512).compact();
  }

  public String generateRefreshToken(UserModel userModel, String deviceId) {
    Instant now = Instant.now();
    return Jwts.builder().subject(userModel.getUsername()).issuer(issuer).audience().add(audience)
        .and().issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(refreshExpiration)))
        .id(UUID.randomUUID().toString()).claim(deviceClaimName, deviceId)
        .claim("purpose", "refresh").signWith(getSignInKey(), Jwts.SIG.HS512).compact();
  }

  public String hashToken(String token) {
    return String.valueOf(token.hashCode());
  }

  public boolean isTokenValid(String token) {
    Claims claims = extractAllClaims(token);
    return claims.getExpiration().after(new Date());
  }

  public Claims extractAccessTokenClaims(String token) {
    try {
      return Jwts.parser().verifyWith(getSignInKey()).requireIssuer(issuer)
          .requireAudience(audience).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      throw new AccessTokenExpiredException(e.getMessage());
    }
  }

  public Claims extractRefreshTokenClaims(String token) {
    try {
      return Jwts.parser().verifyWith(getSignInKey()).requireIssuer(issuer)
          .requireAudience(audience).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      throw new RefreshTokenExpiredException(e.getMessage());
    }
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSignInKey()).requireIssuer(issuer).requireAudience(audience)
        .build().parseSignedClaims(token).getPayload();
  }

  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
