package com.example.chatserver.chat.config;

import com.example.chatserver.chat.service.ChatService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

/*
SimpleWebsocketHandler에서는
(1) 커넥션 되면 Connection Session객체 만들고,
(2) 커넥션이 끊기면 Connection Session 객체를 빼주는 작업을 수동으로 했음
하지만 이러한 작업은 Stomp가 자동으로 알아서 해줌 (Connect 되면 Session 객체 생성하고, 연결 끊기면 알아서 Session 객체 제거해 줌)
StompHandler에서 필요한 작업은 인증 작업!! >> 토큰 검증
*/

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private final ChatService chatService;

    public StompHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    // preSend 메소드 역할: send 하기 전(connect, subscribe, disconnect 등 하기 전)에 무조건 호출됨
    // registration.interceptors(핸들러) 설정했음
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 사용자 요청은 message에 담겨 있으므로 토큰을 꺼내서 토큰 검증 수행
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // FE 코드 보면, Connect 요청 먼저 수행 후에, Subscribe 요청 따로 들어옴. 만약 요청 끊어지면, disconnect 요청 들어옴
        // 사용자요청(accesor)에서 command를 꺼내 어떤 요청인지 파악 (connect, subscibe, disconnect 등)
        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.info("connect 요청시 토큰 유효성 검증");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);
            // 토큰 검증
            Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 서명에 사용된 비밀키 설정
                    .build()
                    .parseClaimsJws(token) // 토큰 파싱 및 서명 검증
                    .getBody(); // 성공하면 Claims 반환, 실패하면 예외 발생
            log.info("토큰 검증 완료");
        }

        if(StompCommand.SUBSCRIBE == accessor.getCommand()) {
            log.info("Subscribe 검증");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String email = claims.getSubject();

            // 요청 url을 통해 참여하고자 하는 roomid를 꺼낼 수 있음
            String roomId = accessor.getDestination().split("/")[2];
            if(!chatService.isRoomParticipant(email, Long.parseLong(roomId))) {
                throw new AuthenticationServiceException("You do not have permission to access this room");
            }
        }

        return message;
    }

}
