package com.example.chatserver.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    public StompWebSocketConfig(StompHandler stompHandler) {
        this.stompHandler = stompHandler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOrigins("http://localhost:3000")
                // ws://가 아닌 http:// 엔드포인트를 사용할 수 있게 해주는 sockJs 라이브러리를 통한 요청을 허용하는 설정 (프론트엔드에서 sockJs 라이브러리 사용시 허용)
                .withSockJS();
	}

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /publish/{roomId} 형태로 메시지 발행해야 함을 설정
        // /publish로 시작하는 url 패턴으로 메시지가 발행되면, @Controller 객체의 @MessageMapping 메서드로 라우팅됨
        registry.setApplicationDestinationPrefixes("/publish");

        // /topic/{roomId} 형태로 메시지를 수신(subscribe_해야 함을 설정
        registry.enableSimpleBroker("/topic");
    }

    // 웹소켓 요청(connect, subscribe, disconnect) 등의 요청시에는 http header 등 http 메시지를 넣어올 수 있고, 이를 꺼낼 수 있음
    // header에 토큰을 넣어올 수 있으므로, connect 같은 요청이 들어오면, 이를 StompHandler에서 interceptor 통해 낚아채고, 토큰을 검증
    // 하지만, 실시간 통신이 맺어지고 나서는 http header를 달고 가지 않음!!
    // 간결한 통신을 위해서 간단한 메시지 형식의 웹소켓 프로토콜 형식의 메시지를 보냄
    // 이미 연결이 맺어진 이후 부터는 검증이 불가능 하므로!! >> 초반 connect 또는 subscribe 할 때, 사용자의 토큰 값을 꺼내서 검증함
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
	}
}


