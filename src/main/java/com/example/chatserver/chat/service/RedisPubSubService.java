package com.example.chatserver.chat.service;

import com.example.chatserver.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;


    public RedisPubSubService(@Qualifier("chatPubSub") StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messageTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;
    }

    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }

    @Override
    // message: 실제로 전달된 메시지
    // pattern: 구독 중인 토픽의 패턴 (패턴 기반으로 다이나믹한 구현 가능- 예: "chat.*" chat으로 시작하는 모든 토픽 )
    public void onMessage(Message message, byte[] pattern) {
        /*
         [ 흐름 과정 ]
         (1) 서버1 -> redis로 메시지 publish 함
         (2) redis는 해당 메시지를 구독중인 서버로 전달함 (서버1, 서버2가 메시지 받음)
         (3) 서버1, 서버2는 메시지를 받아서 처리해야 함
             각 서버는 받은 메시지를 어떻게 해야 하나? Stomp 토픽에 메시지를 발행해야 함
             즉, 서버1과 서버2 각자 갖고 있는 room1에 메시지 전파 해야 함
         */
        /*
         [ 기존 구현 방식 ]
         StompController 코드 보면, 메시지 받았을때 아래 코드를 통해 특정 토픽에 메시지 발행했음
         messageTemplate.convertAndSend("/topic/" + roomId, chatMessageDto);
         */

        // 이제는 Redis를 통해 모든 메시지를 받아서, onMessage()에서 특정 토픽에 메시지 발행하도록 변경
        // 단, 기존에는 chatMessageDto를 파라미터로 받았지만, RedisPubSubService에서는 Message 객체로 받음
        // 따라서, Message 객체에서 필요한 정보를 꺼내서 사용해야 함
        // 문제는 기존 chatMessageDto에는 roomId 정보가 없기 때문에 publish 시에 roomId를 포함해서 publish 해야 함

        String payload = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ChatMessageDto chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class);
            messageTemplate.convertAndSend("/topic/"+chatMessageDto.getRoomId(), chatMessageDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
