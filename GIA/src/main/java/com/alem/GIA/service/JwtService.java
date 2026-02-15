package com.alem.GIA.service;

import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.Permission;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String SECRET_KEY_BASE64 =
            "SGVsbG9IZWxsb0hlbGxvSGVsbG9IZWxsb0hlbGxvSGVsbG9IZWxsbw==";

    /* ================= TOKEN GENERATION ================= */

    public String generateToken(ApplicationUser user,
                                Collection<? extends GrantedAuthority> authorities) {

        Set<String> permissions = resolvePermissions(user);

        return Jwts.builder()
                .subject(user.getUserName())
                .claim("authorities",
                        authorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList())
                .claim("permissions", permissions)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey())
                .compact();
    }

    /* ================= PERMISSIONS ================= */

    private Set<String> resolvePermissions(ApplicationUser user) {
        return user.getRoles().stream()
                .flatMap(role ->
                        role.getPermissions().stream())
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());
    }

    /* ================= EXTRACTION ================= */

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractAuthorities(String token) {
        Object auth =
                extractAllClaims(token).get("authorities");

        if (auth instanceof Collection<?> col) {
            return col.stream()
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }

    public List<String> extractPermissions(String token) {
        Object perms =
                extractAllClaims(token).get("permissions");

        if (perms instanceof Collection<?> col) {
            return col.stream()
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }

    /* ================= VALIDATION ================= */

    public boolean validateToken(String token,
                                 org.springframework.security.core.userdetails.UserDetails userDetails) {
        return extractUsername(token)
                .equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token)
                .before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token,
                Claims::getExpiration);
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> resolver) {
        return resolver.apply(
                extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64
                        .decode(SECRET_KEY_BASE64));
    }
}
