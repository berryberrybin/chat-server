//package com.example.chatserver.chat.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//// connect로 웹소켓 연결요청이 들어오면, 이를 처리할 클래스
//// 연결한 세션 관리: thread-safe한 Set 사용
//@Component
//@Slf4j
//public class SimpleWebsocketHandler extends TextWebSocketHandler {
//
//    // 웹소켓 연결된 사용자 정보를 저장할 Set 자료구조 -> Thread-Safe한 ConcurrentHashMap을 사용하여 Set 자료구조를 생성
//    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
//
//    // 웹소켓 연결 요청이 들어오면, 이 메서드가 호출됨 -> 메모리(Set 자료구조)에 사용자 연결 정보 저장
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        sessions.add(session);
//        log.info("WebSocket 연결됨: " + session.getId());
//    }
//
//    // 사용자에게 메시지를 전파하는 메소드
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        log.info("받은 메시지: " + payload);
//        for(WebSocketSession s : sessions) {
//            if (s.isOpen()) {
//                s.sendMessage(new TextMessage(payload));
//            }
//        }
//    }
//
//    // 웹소켓 연결이 끊어지면, 이 메서드가 호출됨 -> 세션을 메모리에서 삭제
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        sessions.remove(session);
//        log.info("WebSocket 연결 끊어짐: " + session.getId());
//    }
//
//}
