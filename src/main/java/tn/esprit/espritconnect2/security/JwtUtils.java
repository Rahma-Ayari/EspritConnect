package tn.esprit.espritconnect2.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_MFA_PENDING = "MFA_PENDING";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.mfa-pending-expiration-ms:300000}")
    private long mfaPendingExpirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .claim("roles", userDetails.getAuthorities()
                        .stream().map(a -> a.getAuthority()).toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    /** Jeton court (5 min) émis après email/mot de passe valides, requis pour /verify-2fa-login. */
    public String generateMfaPendingToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_MFA_PENDING)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + mfaPendingExpirationMs))
                .signWith(key())
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        String type = parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
        return type != null ? type : TOKEN_TYPE_ACCESS;
    }

    public boolean isMfaPendingToken(String token) {
        return TOKEN_TYPE_MFA_PENDING.equals(extractTokenType(token));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            if (isMfaPendingToken(token)) {
                return false;
            }
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateMfaPendingToken(String token, String expectedEmail) {
        try {
            if (!isMfaPendingToken(token)) {
                return false;
            }
            Claims claims = parseClaims(token);
            return expectedEmail.equalsIgnoreCase(claims.getSubject()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("MFA pending JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload();
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }
}
