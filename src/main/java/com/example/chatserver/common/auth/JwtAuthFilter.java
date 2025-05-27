package com.example.chatserver.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter extends GenericFilter {

    @Value("${jwt.secretKey}")
    private String secretKey;

    // 사용자가 요청할 때 JWT 토큰을 갖고 있으면, "토큰을 검증" (서버에서 만든 토큰인지 확인하는 로직)
    // 기본적으로 request, response가 주어짐

    // chain은 다시 FilterChain으로 돌아갈 때 사용하기 위함 (chain.doFilter(request, response)호출을 통해 다시 FilterChain으로 돌아감)
    // SecurityConfigs에서 JwtAuthFilter를 등록했기 때문에, URL 요청 들어오면 JwtAuthFilter가 실행됨
    // doFilter 수행 후 다시 securityConfigs로 돌아가야지만 Controller로 요청이 들어감
    // chain.doFilter를 호출하지 않으면 FilterChain으로 돌아가지 않음 (즉, Controller로 요청이 안들어감)

    // request안에 JWT 토큰이 들어있음! request에서 토큰을 꺼내서 토큰을 검증함
    // 정상이면 doFilter로 이동 (즉, FilterChain으로 돌아감) + 비정상이면 에러 메시지를 response에 담아서 반환

    // 정상일 경우, Authentication 객체를 생성해서 담아줘야 함
    // 만약 Authentication 객체가 비어져 있으면 에러가 발생함 + 단,예외 상황인 경우(로그인, 회원가입 등)는 Authentication 객체가 비어져 있어도 통과됨

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String token = httpRequest.getHeader("Authorization"); // Authorization 헤더에서 JWT 토큰을 가져옴
        try {
            if (token != null) {
                // Baser 인증 방식 사용
                if (!token.substring(0,7).equals("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer Token이 아닙니다.");
                }
                // Bearer 떼고, 토큰 원본만 꺼냄
                String jwtToken = token.substring(7);

                // 토큰 검증 및 Cliams 추출
                // 어떻게 토큰 검증할 것인가?
                // 주어진 token을 다시 암호화 해서 일치여부 확인 (header+payLoad를 꺼내서, secreteKey를 붙여 다시 암호화)
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(jwtToken)// 여기까지 토큰 검증 -> 검증 실패시 exception 발생 (try-catch로 잡아줌)
                        .getBody(); // getBody()를 통해 정보 payload를 꺼낼 수 있음 (payload = claims)
                // claims를 꺼낸 이유는 Authentication 객체를 만들기 위해서!

                // Authentication 객체 생성 (subject, password, authorities로 구성됨)
                // List<GrantedAuthority> authorities: 권한 객체 생성 (여러 가지 권한이 있을 수 있기 때문에 기본적으로 List 형태로 주고 받음)
                // calims.getSubject() : email 값을 가져옴
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE" + claims.get("role").toString())); // claims에서 role을 꺼내서 권한으로 사용 (관례적으로 ROLE_이 붙어있음)
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", authorities);

                // 스프링에서 Authentication 객체를 SecurityContextHolder > SecurityContext > Authentication에 담아줌
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            chain.doFilter(request, response); // chain으로 돌아가라는 의미
            // Authentication 객체가 비어져 있으면 바로 chain.doFilter 수행된 후, SecurityFilterChain에서 에러 발생함
        } catch (Exception e) {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("invalid token");
        }
    }
}
