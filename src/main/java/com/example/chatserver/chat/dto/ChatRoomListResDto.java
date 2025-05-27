package com.example.chatserver.chat.dto;


import com.example.chatserver.chat.domain.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomListResDto {
    private long roomId;
    private String roomName;

    public static ChatRoomListResDto fromEntity(ChatRoom chatRoom) {
        return ChatRoomListResDto.builder()
                .roomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .build();
    }
}
