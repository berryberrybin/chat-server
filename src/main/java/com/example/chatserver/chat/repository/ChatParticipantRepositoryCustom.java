package com.example.chatserver.chat.repository;

import com.example.chatserver.chat.domain.ChatRoom;

import java.util.Optional;

public interface ChatParticipantRepositoryCustom {
    Optional<ChatRoom> findExistingPrivateRoom(Long userId, Long partnerId);
}
