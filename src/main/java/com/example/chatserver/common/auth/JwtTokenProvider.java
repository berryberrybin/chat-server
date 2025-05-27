package com.example.chatserver.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final int expiration;
    private Key SECRETE_KEY;

    // @Value("${jwt.secretKey}"): application.yml의 jwt.secretKey 값 사용
    // SECRETE_KEY : secreteKey는 인코딩 되어 있기 때문에 Base64로 먼저 디코딩 후, 암호화 수행 (암호화 알고리즘으로 HS512 사용)
    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey, @Value("${jwt.expiration}") int expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
        this.SECRETE_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
    }

    // Claims : JWT의 payload에 들어가는 정보
    // Claims에는 반드시 subject 요소가 포함됨 -> subject는 claims의 key 역할을 하는 변수값 (즉, email을 claims의 키값으로 사용)
    public String createToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L))
                .signWith(SECRETE_KEY)
                .compact();
        return token;
    }

    // JWT 토큰 검증
    public boolean validateToken(String token) {
        // JWT 토큰 검증 로직 구현
        return true;
    }

    // JWT 토큰에서 이메일 추출
    public String getEmailFromToken(String token) {
        // JWT 토큰에서 이메일 추출 로직 구현
        return "";
    }
}
