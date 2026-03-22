package ru.fshs.tour.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.fshs.tour.config.SecurityProperties;
import ru.fshs.tour.exception.InvalidTokenException;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";

    private final SecurityProperties securityProperties;

    public String generateAccessToken(AppUserPrincipal principal) {
        return generateToken(principal, JwtTokenType.ACCESS, securityProperties.jwt().accessExpirationMs());
    }

    public String generateRefreshToken(AppUserPrincipal principal) {
        return generateToken(principal, JwtTokenType.REFRESH, securityProperties.jwt().refreshExpirationMs());
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        var claims = extractAllClaims(token);
        return claims.getSubject().equals(userDetails.getUsername())
                && JwtTokenType.ACCESS.name().equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                && !isExpired(claims);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        var claims = extractAllClaims(token);
        return claims.getSubject().equals(userDetails.getUsername())
                && JwtTokenType.REFRESH.name().equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                && !isExpired(claims);
    }

    private String generateToken(AppUserPrincipal principal, JwtTokenType tokenType, long expirationMs) {
        var now = Instant.now();
        var claims = Map.<String, Object>of(
                TOKEN_TYPE_CLAIM, tokenType.name(),
                "uid", principal.getId().toString(),
                "role", principal.getUserType()
        );

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(principal.getUsername())
                .setIssuer(securityProperties.jwt().issuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .requireIssuer(securityProperties.jwt().issuer())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException
                 | SignatureException | IllegalArgumentException exception) {
            throw new InvalidTokenException("Invalid or expired token");
        }
    }

    private boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private Key getSigningKey() {
        var keyBytes = Decoders.BASE64.decode(securityProperties.jwt().secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
