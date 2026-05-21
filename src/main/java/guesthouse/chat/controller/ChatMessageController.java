package guesthouse.chat.controller;

import guesthouse.chat.dto.request.ChatMessageSendRequest;
import guesthouse.chat.dto.response.ChatMessageDto;
import guesthouse.chat.dto.response.ChatMessagesResponse;
import guesthouse.chat.service.ChatService;
import guesthouse.common.annotation.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/rooms/{roomId}/messages")
public class ChatMessageController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<ChatMessagesResponse> getMessages(
            @UserId Long userId,
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int pageNumber
    ) {
        return ResponseEntity.ok(new ChatMessagesResponse(chatService.getMessages(userId, roomId, pageNumber)));
    }

    @PostMapping
    public ResponseEntity<ChatMessageDto> sendMessage(
            @UserId Long userId,
            @PathVariable Long roomId,
            @Valid @RequestBody ChatMessageSendRequest request
    ) {
        return ResponseEntity.ok(chatService.sendMessage(userId, roomId, request));
    }
}
