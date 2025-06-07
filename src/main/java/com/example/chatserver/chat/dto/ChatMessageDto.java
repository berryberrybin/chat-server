package com.example.chatserver.chat.dto;

import com.example.chatserver.chat.domain.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private String message;
    private String senderEmail;
    private LocalDateTime createdTime;

    // redis를 통해 메시지를 발행할 때 사용하기 위한 roomId 추가
    private Long roomId;

    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .message(chatMessage.getContent())
                .senderEmail(chatMessage.getMember().getEmail())
                .createdTime(chatMessage.getCreatedTime())
                .build();
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
