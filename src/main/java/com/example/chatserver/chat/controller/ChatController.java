package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.dto.MyChatListResDto;
import com.example.chatserver.chat.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 그룹 채팅방 개설
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom(@RequestParam String roomName) {
        chatService.createGroupRoom(roomName);
        return ResponseEntity.ok().build();
    }

    // 그룹 채팅 목록 조회
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getGroupChatRooms() {
        List<ChatRoomListResDto> chatRooms = chatService.getGroupChatRooms();
        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
    }

    // 그룹 채팅방 참여
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable Long roomId) {
        chatService.addParticipantToGroupChat(roomId);
        return ResponseEntity.ok().build();
    }

    // 이전 메시지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getGroupChatHistory(@PathVariable Long roomId) {
        List<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(roomId);
        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
    }

    // 채팅메시지 읽음 처리
    /*
    - [ 방법 1 ] 복잡한 방법
    - A가 메시지 보내는 시점에 B만 채팅방에 들어와 있고(= subscribe 하고 있음), C는 채팅방에 없을 경우
    - A,B 둘다 읽음 처리, C는 미읽음 처리
    - 보내는 메시지 하나하나 누가 subscribe 하고 있는가? 직접 세션을 관리해줘야 함
    - subscribe 하자마자 서버의 메모리에 누가 현재 이 서버에 connect를 맺고 subscribe 하고 있는지
      특정 room을 따져가면서 해당 room에 누가 subscribe 했는지를 관리해야 함
    */
    /*
    - [ 방법 2 ] 단순한 방법
    - disconnect 하는 시점 => 화면 끄거나(unmount), route를 이동하거나(다른 탭으로 이동)
    - disconnect가 되었다는 의미는 현재 화면에 접속해있고, subscribe 하고 있다는 의미
    - 현재까지 받은 메시지를 모두 읽음 처리 해버림
    - 메시지 보내는 시점에 현재 누가 들어와있는지 판단하는 것이 아닌
      화면 끄고 나가는 시점에 현재까지 채팅방에 쌓인 메시지를 모두 읽음 처리 함
    */
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> readGroupChatRoom(@PathVariable Long roomId) {
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

    // 내 채팅방 목록 조회 (roomId, roomName, 그룹채팅방여부, 읽지않은 메시지 개수)
    // 그룹 채팅방 여부를 통해 그룹 채팅방인 경우만 나가기 가능하도록 함
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms() {
        List<MyChatListResDto> myChatListResDtos = chatService.getMyChatRooms();
        return new ResponseEntity<>(myChatListResDtos, HttpStatus.OK);
    }

    // 채팅방 나가기
    @DeleteMapping("/room/group/{roomId}/leave")
    public ResponseEntity<?> leaveGroupChatRoom(@PathVariable Long roomId) {
        chatService.leaveGroupChatRoom(roomId);
        return ResponseEntity.ok().build();
    }

    // 개인 채팅방 개설 또는 기존 roomId 반환
    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam Long chatPartnerId) {
        System.out.println("chatPartnerId: " + chatPartnerId);
        Long roomId = chatService.getOrCreatePrivateRoom(chatPartnerId);
        System.out.println("Private room ID: " + roomId);
        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }

}
