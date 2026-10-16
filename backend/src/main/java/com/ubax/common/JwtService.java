package com.ubax.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${ubax.jwt.secret}") String secret,
                      @Value("${ubax.jwt.expiration-minutes}") long expirationMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            for (int i = bytes.length; i < 32; i++) padded[i] = (byte) ('0' + (i % 10));
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMinutes = expirationMinutes;
    }

    public String generate(Long userId, Long tenantId, String username, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMinutes * 60_000);
        Map<String, Object> claims = new HashMap<>();
        if (tenantId != null) claims.put("tenantId", tenantId);
        claims.put("username", username);
        claims.put("roles", roles);
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public TenantContext.Principal parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Long userId = Long.valueOf(c.getSubject());
        Number tenantId = (Number) c.get("tenantId");
        String username = (String) c.get("username");
        List<String> roles = (List<String>) c.get("roles");
        return new TenantContext.Principal(userId, tenantId == null ? null : tenantId.longValue(), username, roles);
    }
}
