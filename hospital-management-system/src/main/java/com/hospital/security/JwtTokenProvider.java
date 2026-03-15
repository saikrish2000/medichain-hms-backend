package com.hospital.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return buildToken(userPrincipal.getId().toString(), jwtExpiration,
                userPrincipal.getRole(), userPrincipal.getFullName());
    }

    public String generateTokenFromUserId(Long userId, String role, String fullName) {
        return buildToken(userId.toString(), jwtExpiration, role, fullName);
    }

    public String generateRefreshToken(Long userId) {
        return buildToken(userId.toString(), refreshExpiration, null, null);
    }

    private String buildToken(String subject, long expiry, String role, String fullName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiry);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256);

        if (role != null)     builder.claim("role", role);
        if (fullName != null) builder.claim("name", fullName);

        return builder.compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException ex)  { log.error("Invalid JWT signature"); }
        catch (MalformedJwtException ex) { log.error("Invalid JWT token"); }
        catch (ExpiredJwtException ex)   { log.error("Expired JWT token"); }
        catch (UnsupportedJwtException ex){ log.error("Unsupported JWT token"); }
        catch (IllegalArgumentException ex){ log.error("JWT claims string is empty"); }
        return false;
    }
}
