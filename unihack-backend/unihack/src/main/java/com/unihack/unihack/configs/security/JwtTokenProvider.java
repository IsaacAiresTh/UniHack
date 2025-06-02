package com.unihack.unihack.configs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyPlain;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationInMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Na versão 0.12.x, use SecretKey em vez de Key
        this.key = Keys.hmacShaKeyFor(secretKeyPlain.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // MÉTODO ATUALIZADO PARA JJWT 0.12.x
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key) // Mudança: verifyWith em vez de setSigningKey
                    .build()
                    .parseSignedClaims(token) // Mudança: parseSignedClaims em vez de parseClaimsJws
                    .getPayload(); // Mudança: getPayload em vez de getBody
        } catch (Exception e) {
            System.err.println("Erro ao parsear token JWT: " + e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // Mudança: claims em vez de setClaims
                .subject(subject) // Mudança: subject em vez de setSubject
                .issuedAt(new Date(System.currentTimeMillis())) // Mudança: issuedAt em vez de setIssuedAt
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMs)) // Mudança: expiration em vez de setExpiration
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}