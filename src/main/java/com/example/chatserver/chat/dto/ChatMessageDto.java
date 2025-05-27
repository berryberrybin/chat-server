package com.example.chatserver.chat.dto;

import com.example.chatserver.chat.domain.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private String message;
    private String senderEmail;

    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .message(chatMessage.getContent())
                .senderEmail(chatMessage.getMember().getEmail())
                .build();
    }
}
