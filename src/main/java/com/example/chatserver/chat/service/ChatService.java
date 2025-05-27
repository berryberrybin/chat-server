package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.dto.MyChatListResDto;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, ReadStatusRepository readStatusRepository, MemberRepository memberRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.readStatusRepository = readStatusRepository;
        this.memberRepository = memberRepository;
    }

    public void saveMessage(Long roomId, ChatMessageDto chatMessageDto) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        // 보낸 사람 조회
        Member sender = memberRepository.findByEmail(chatMessageDto.getSenderEmail())
                .orElseThrow(() -> new EntityNotFoundException("sender cannot be found"));

        // 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        // 사용자별 읽음 여부 저장
        // chatRoom에 참여자 목록 조회 후, 발송자는 읽음 처리, 그외 모든 참여자는 읽지않음 으로 저장
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant participant : chatParticipants) {
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(participant.getMember())
                    .chatMessage(chatMessage)
                    .isRead(participant.getMember().equals(sender))
                    .build();
            readStatusRepository.save(readStatus);
        }

    }

    public void createGroupRoom(String chatRoomName) {
        // 룸 생성자에 대한 정보는 없더라고, 룸 생성자가 chatParticipant 참여자로 등록은 되어야 함
        // JwtAuthFilter에서 성공적으로 Login 하면 (= 정상적인 token 갖고 있으면), Authentication 객체를 생성
        // SecurityContextHolder에서 사용자 정보(email)을 꺼낼 수 있음
        String loginUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member roomCreator = memberRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("roomCreator cannot be found"));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(chatRoomName)
                .isGroupChat("Y")
                .build();
        chatRoomRepository.save(chatRoom);
        // 개설자를 채탕참여자로 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(roomCreator)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatRoomListResDto> getGroupChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
        return chatRooms.stream()
                .map(ChatRoomListResDto::fromEntity)
                .collect(Collectors.toList());
    }

    public void addParticipantToGroupChat(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        String loginUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member loginUser = memberRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("loginUser cannot be found"));

        // 개인 채팅방은 중도에 참여자가 참여 불가능
        if (chatRoom.getIsGroupChat().equals("N")) {
            throw new IllegalArgumentException("chat room is not a group type");
        }

        // 이미 참여자인지 검증
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, loginUser);
        if (!participant.isPresent()) {
            addParticipantToRoom(chatRoom, loginUser);
        }

    }

    // ChatParticipant 객체 생성 후 저장
    public void addParticipantToRoom(ChatRoom chatRoom, Member member) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
        System.out.println("---addParticipantToRoom [ member ] " + member.getId());
    }

    public List<ChatMessageDto> getChatHistory(Long roomId) {
        // 해당 채팅방의 참여자가 아닐 경우 예외 처리
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        String loginUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member loginUser = memberRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("loginUser cannot be found"));
        boolean isParticipant = chatRoom.getChatParticipants().stream()
                .anyMatch(participant -> participant.getMember().equals(loginUser));

        if (!isParticipant) {
            throw new IllegalArgumentException("You are not a participant of this chat room.");
        }
        // 특정 room의 message 조회
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        return chatMessages.stream()
                .map(ChatMessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    public boolean isRoomParticipant(String email, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));
        return chatRoom.getChatParticipants().stream()
                .anyMatch(participant -> participant.getMember().equals(member));
    }

    public void messageRead(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        String loginUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member loginUser = memberRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("loginUser cannot be found"));

        List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndMember(chatRoom, loginUser);
        for (ReadStatus readStatus : readStatuses) {
            readStatus.updateIsRead(true);
        }
    }

    public List<MyChatListResDto> getMyChatRooms() {
        String loginUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member loginUser = memberRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("loginUser cannot be found"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMember(loginUser);
        List<MyChatListResDto> chatListResDtos = new ArrayList<>();
        for (ChatParticipant chatParticipant : chatParticipants) {
            ChatRoom chatRoom = chatParticipant.getChatRoom();
            Long count = readStatusRepository.countByChatRoomAndMemberAndIsReadFalse(chatRoom, loginUser);
            MyChatListResDto dto = MyChatListResDto.builder()
                    .roomId(chatRoom.getId())
                    .roomName(chatRoom.getName())
                    .isGroupChat(chatRoom.getIsGroupChat())
                    .unReadCount(count)
                    .build();
            chatListResDtos.add(dto);
        }
        return chatListResDtos;
    }

    // 단체 채팅방 나가면,
    // (1) 참여자 객체 삭제 (단, 메시지, 읽음 여부는 그대로)
    // (2) 모든 참여자가 단체 채팅방을 나갈 경우, 채팅방 자체를 지우고, 메시지, 읽음여부 등 모두 삭제
    public void leaveGroupChatRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        String loginUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member loginUser = memberRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("loginUser cannot be found"));

        // 개인 채팅방은 나가는게 의미가 없으므로 그룹채팅방만 나갈 수 있게 함
        if (chatRoom.getIsGroupChat().equals("N")) {
            throw new IllegalArgumentException("chat room is not a group type");
        }

        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, loginUser)
                .orElseThrow(() -> new EntityNotFoundException("chatParticipant cannot be found"));
        chatParticipantRepository.delete(chatParticipant);

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        if (chatParticipants.isEmpty()) {
            chatRoomRepository.delete(chatRoom); // cascade 설정 및 orphan removal 설정으로 모든 연관 데이터 삭제됨
        }
    }

    public Long getOrCreatePrivateRoom(Long chatPartnerId) {
        // 개인 채팅방은 참여자가 2명만 존재
        // 참여자 2명이 모두 참여하는 채팅방이 있는지 확인
        String loginUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member loginUser = memberRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("loginUser cannot be found"));

        System.out.println("loginUser: " + loginUser.getId());
        Member chatPartner = memberRepository.findById(chatPartnerId)
                .orElseThrow(() -> new EntityNotFoundException("chatPartner cannot be found"));

        System.out.println("chatPartner: " + chatPartner.getId());
        Optional<ChatRoom> originChatRoom = chatParticipantRepository.findExistingPrivateRoom(loginUser.getId(), chatPartner.getId());
        if(originChatRoom.isPresent()) {
            return originChatRoom.get().getId();
        }
        // 이미 생성된 1:1 채팅방이 없을 경우, 신규 개인 채팅방 생성
        ChatRoom privateChatRoom = ChatRoom.builder()
                .name(loginUser.getName() + " & " + chatPartner.getName())
                .isGroupChat("N")
                .build();

        chatRoomRepository.save(privateChatRoom);

        // 참여자 추가
        addParticipantToRoom(privateChatRoom, loginUser);
        addParticipantToRoom(privateChatRoom, chatPartner);

        return privateChatRoom.getId();
    }
}
