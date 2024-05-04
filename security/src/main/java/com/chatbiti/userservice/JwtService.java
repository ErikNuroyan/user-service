package com.chatbiti.userservice;

import com.chatbiti.userservice.model.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtService {
    @Value("${application.security.key}")
    private String key;

    @Value("${application.security.token-duration}")
    private long tokenDuration;

    private TokenRepository tokenRepository;

    public JwtService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public String generateToken(UserDetails userDetails, Map<String, Object> claims) {
        List<String> authorities = userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        var currentTime = System.currentTimeMillis();
        return Jwts
                .builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + tokenDuration))
                .claim("authorities", authorities)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        return isValidUser(token, userDetails)
               && !isTokenExpired(token)
               && tokenRepository.existsByToken(token);
    }

    public boolean isValidUser(String token, UserDetails userDetails) {
        return extractEmail(token).equals(userDetails.getUsername());

    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    public void deleteToken(String token) {
        Optional<Token> storedToken = tokenRepository.findByToken(token);
        storedToken.ifPresent(value -> tokenRepository.delete(value));
    }


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

}
