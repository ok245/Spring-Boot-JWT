package com.app.utilis;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private String SECRET_KEY="ddfnsdfsdfjijsdbfiabfiasudbfiasbdifbas";

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();

    }

    public String generateToken(String userName) {
        Map<String,Object> claims=new HashMap<>();
       return createToken(claims,userName);

    }

    public String createToken(Map<String,Object> claims,String subject){
        return  Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ","JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+1000 *60 * 5))
                .signWith(getSignInKey())
                .compact();

    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
