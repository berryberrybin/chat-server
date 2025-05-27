//package com.example.chatserver.chat.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    private final SimpleWebsocketHandler simpleWebsocketHandler;
//
//    public WebSocketConfig(SimpleWebsocketHandler simpleWebsocketHandler) {
//        this.simpleWebsocketHandler = simpleWebsocketHandler;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        // "/connect" URL로 WebSocket 연결 요청 들어오면, 핸들러 클래스가 처리
//        registry.addHandler(simpleWebsocketHandler, "/connect")
//                // SecurityConfig에서의 CORS 예외 설정한 것은 "http 요청"에 대해서만 예외처리 적용됨
//                // -> 따라서 websocket 프로토콜에 대한 요청에 대해서는 별도의 CORS 설정이 필요함
//                .setAllowedOrigins("http://localhost:3000"); // CORS 예외 처리
//    }
//
//}
