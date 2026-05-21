package guesthouse.chat.controller;

import guesthouse.chat.dto.request.ChatRoomCreateRequest;
import guesthouse.chat.dto.response.ChatRoomCreateResponse;
import guesthouse.chat.dto.response.ChatRoomsResponse;
import guesthouse.chat.service.ChatService;
import guesthouse.common.annotation.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/rooms")
public class ChatRoomController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatRoomCreateResponse> createRoom(
            @UserId Long userId,
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        Long roomId = chatService.createRoom(userId, request);
        return ResponseEntity.ok(new ChatRoomCreateResponse(roomId));
    }

    @GetMapping
    public ResponseEntity<ChatRoomsResponse> getRooms(@UserId Long userId) {
        return ResponseEntity.ok(new ChatRoomsResponse(chatService.getRooms(userId)));
    }

    @PatchMapping("/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @UserId Long userId,
            @PathVariable Long roomId
    ) {
        chatService.markAsRead(userId, roomId);
        return ResponseEntity.ok().build();
    }
}
