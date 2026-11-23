package com.gamehub.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Genera y valida los tokens JWT (HS256). El payload lleva el email (sub) y el rol. */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long duracionMs;

    public JwtService(@Value("${app.jwt.secreto}") String secreto,
                      @Value("${app.jwt.duracion-ms}") long duracionMs) {
        // El secreto debe tener al menos 32 caracteres para HS256
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionMs = duracionMs;
    }

    public String generarToken(String email, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave)
                .compact();
    }

    /** Lanza JwtException si el token es inválido, fue alterado o venció. */
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
