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

    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .message(chatMessage.getContent())
                .senderEmail(chatMessage.getMember().getEmail())
                .createdTime(chatMessage.getCreatedTime())
                .build();
    }
}
