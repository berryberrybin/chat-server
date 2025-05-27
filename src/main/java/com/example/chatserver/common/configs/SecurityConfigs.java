package com.example.chatserver.common.configs;


import com.example.chatserver.common.auth.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfigs {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfigs(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // 기본적으로 같은 도메인이 아니면 통신이 안되므로, cors 설정 통해 허용할 주소 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF 비활성화 (보안공격 대비하지 않겠다 -> 대신 서비스 코드에서 검증처리)
                .csrf(AbstractHttpConfigurer::disable)

                // http Basic 비활성화 (보안 인증 방법 중 하나인 http Basic을 사용하지 않겠다 -> 대신 토큰 기반의 인증처리)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 특정 URL에 대한 인증 처리 제외 설정 (Authentication 객체 요구하지 않는 URL 설정)
                . authorizeHttpRequests(
                         // 인증처리가 필요 없는 요청 (홈, 로그인, 회원가입은 인증 없이 접근 허용하겠다)
                        //auth -> auth.requestMatchers("/member/create", "/", "/member/doLogin", "/connect/**"). permitAll()
                        // sockJs 사용하면서 /connect/info?t=XXX 와 같이 뒤에 특정 값을 더 넣어주기 때문에 403에러 발생 -- > /connect/**로 변경하여 해결
                        auth -> auth.requestMatchers("/member/create", "/", "/member/doLogin", "/connect/**"). permitAll()
                        // 그 외의 요청은 인증 처리 하겠다
                        .anyRequest().authenticated()
                )

                // session 방식을 사용하지 않겠다 (JWT를 사용하기 때문에 session을 사용하지 않겠다)
                // JWT는 토큰 자체에 사용자 정보와 인증 상태를 담고 있어 서버에 세션을 유지할 필요가 없음 (Stateless 설정은 서버 부하를 줄임)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 어떤 검증 코드 사용할지 설정 (JwtAuthFilter에 어떤 방식으로 토큰 검증할지 로직 작성해 둠)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("*")); // 모든 HTTP 메서드 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더값 허용
        configuration.setAllowCredentials(true); // 자격 증명 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 Url 패턴에 CORS 설정 적용
        return source;
    }

    @Bean
    public PasswordEncoder makePassword() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
