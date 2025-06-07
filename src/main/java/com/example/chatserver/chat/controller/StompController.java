package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.service.ChatService;
import com.example.chatserver.chat.service.RedisPubSubService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;
    private final RedisPubSubService redisPubSubService;

    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService, RedisPubSubService redisPubSubService) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
        this.redisPubSubService = redisPubSubService;
    }

    /*
    // [방법 1] MessageMapping(수신)과 SendTo(topic에 메시지 전달) 한꺼번에 처리
    @MessageMapping("/{roomId}") // client에서 특정 publish/roomId 형태로 메시지 발행시 MessageMapping 수신
    @SendTo("/topic/{roomId}") // 해당 roomId에 메시지를 발행하여 구독중인 client에게 메시지 전송
    // @DestinationVariable: @MessageMapping 어노테이션으로 정의된 Websocket Controller내에서만 사용됨
    public String sendMessage( @DestinationVariable Long roomId, String message) {
        log.info(message);
        return message;
    }
*/

    // [방법 2] MessageMapping 어노테이션만 활용하는 방법
//    @MessageMapping("/{roomId}")
//    public void sendMessage( @DestinationVariable Long roomId, ChatMessageDto chatMessageDto) {
//        log.info(chatMessageDto.getMessage());
//        chatService.saveMessage(roomId, chatMessageDto);
//        messageTemplate.convertAndSend("/topic/" + roomId, chatMessageDto);
//    }

    // [방법 3] redis를 통해 메시지를 발행 적용
    @MessageMapping("/{roomId}")
    public void sendMessage( @DestinationVariable Long roomId, ChatMessageDto chatMessageDto)
            throws JsonProcessingException {
        log.info(chatMessageDto.getMessage());
        chatService.saveMessage(roomId, chatMessageDto);
        chatMessageDto.setRoomId(roomId); // roomId를 chatMessageDto에 설정 (message에 포함되도록)

        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(chatMessageDto);
        redisPubSubService.publish("chat", message);
    }

}





