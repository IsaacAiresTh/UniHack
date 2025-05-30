package com.unihack.unihack.configs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
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

    private Key key; // Chave HMAC-SHA para assinar o token

    @PostConstruct
    public void init() {
        // Transforma a string da chave secreta em um objeto Key seguro para HMAC-SHA
        this.key = Keys.hmacShaKeyFor(secretKeyPlain.getBytes());
    }

    // Extrai o nome de usuário (matrícula) do token JWT
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrai a data de expiração do token JWT
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrai uma claim específica usando uma função resolver
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrai todas as claims do token JWT
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // Verifica se o token JWT expirou
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Gera um token JWT para um UserDetails (usuário)
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Pode adicionar mais claims aqui se precisar, por exemplo, papéis (roles)
        // claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        return createToken(claims, userDetails.getUsername()); // Username aqui será a matrícula
    }

    // Cria o token JWT com as claims e o nome de usuário (matrícula)
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Define o "dono" do token (matrícula)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Data de criação
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs)) // Data de expiração
                .signWith(key, SignatureAlgorithm.HS256) // Assina com a chave e algoritmo
                .compact();
    }

    // Valida o token JWT (verifica se o nome de usuário é o mesmo e se não expirou)
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}