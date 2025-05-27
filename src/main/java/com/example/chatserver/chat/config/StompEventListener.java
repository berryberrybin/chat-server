package com.example.chatserver.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/* 클래스 역할
    - SimpleWebsocket에서는 직접 Session을 관리하였지만, Stomp에서는 직접 관리하지 않음
    - client와 server간 연결 맺은 연결 객체를 관리하지 않음
    - StompEventListener를 통해 발생한 event를 catch해서 로그, 디버깅 목적으로 사용하기 위함
    - 실시간 통신에서 가장 유의해야 할 것 >> 너무 연결 객체가 많아져 서버에서 과부하 생기는 것
      (특정 Room에 수천명의 사용자가 connection 맺어져있고, 적절하게 connection이 제거되지 않는 경우)
      즉, 적절하게 connection이 잘 제거되고 있는지 철저하게 테스트 필요함
*/
// Spring 과 Stomp는 기본적으로 Session 관리를 자동으로 처리함 (개발자가 직접 session을 처리하지 않고, 내부적으로 처리해 줌)
// 연결, 해제 이벤트를 기록하고, 연결된 세션수를 실시간으로 확인할 목적으로 EventListener를 생성하였음
// 직접 session을 관리하기 위해서 만들어진 것이 아님!!
@Component
@Slf4j
public class StompEventListener {
    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    // SessionConnectedEvent를 파라미터로 받았으므로, connect 요청이 발생했을 때 해당 이벤트가 발생하게 됨
    @EventListener
    public void  connectHandler(SessionConnectedEvent event) {
        // event에는 사용자의 요청 정보(header, token 정보 등) 들어있음
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId());
        log.info("connect session Id: " + accessor.getSessionId());
        log.info("total session: " + sessions.size());
    }

    @EventListener
    public void  disconnectHandler(SessionDisconnectEvent event) {
        // event에는 사용자의 요청 정보(header, token 정보 등) 들어있음
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.remove(accessor.getSessionId());
        log.info("disconnect session Id: " + accessor.getSessionId());
        log.info("total session: " + sessions.size());
    }
}
