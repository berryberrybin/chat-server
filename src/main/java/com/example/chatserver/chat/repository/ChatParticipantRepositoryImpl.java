package com.example.chatserver.chat.repository;

import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.QChatParticipant;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatParticipantRepositoryImpl implements ChatParticipantRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ChatRoom> findExistingPrivateRoom(Long userId, Long partnerId) {
        QChatParticipant cp1 = QChatParticipant.chatParticipant;
        QChatParticipant cp2 = new QChatParticipant("cp2");

        ChatRoom result = queryFactory
                .select(cp1.chatRoom)
                .from(cp1)
                .join(cp2).on(cp1.chatRoom.id.eq(cp2.chatRoom.id))
                .where(
                        cp1.member.id.eq(userId),
                        cp2.member.id.eq(partnerId),
                        cp1.chatRoom.isGroupChat.eq("N")
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
