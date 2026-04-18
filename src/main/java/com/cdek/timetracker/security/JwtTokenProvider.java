package com.cdek.timetracker.security;

import com.cdek.timetracker.config.JwtProperties;
import com.cdek.timetracker.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
        this.expirationMs = jwtProperties.getExpirationMs();
    }

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMs);
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim(CLAIM_USER_ID, principal.getId())
                .claim(CLAIM_ROLE, principal.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public Authentication buildAuthentication(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
        Long userId = claims.get(CLAIM_USER_ID, Long.class);
        String username = claims.getSubject();
        String roleRaw = claims.get(CLAIM_ROLE, String.class);
        UserRole role = UserRole.valueOf(roleRaw);

        UserPrincipal principal = new UserPrincipal(userId, username, "", role);
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    public long getExpirationSeconds() {
        return Duration.ofMillis(expirationMs).toSeconds();
    }
}
