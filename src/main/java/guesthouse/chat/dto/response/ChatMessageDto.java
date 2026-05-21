package guesthouse.chat.dto.response;

import guesthouse.chat.domain.model.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageDto(
        Long id,
        Long chatRoomId,
        Long senderId,
        String content,
        Boolean isRead,
        LocalDateTime createdAt
) {
    public static ChatMessageDto from(ChatMessage message) {
        return new ChatMessageDto(
                message.getId(),
                message.getChatRoomId(),
                message.getSenderId(),
                message.getContent(),
                message.getIsRead(),
                message.getCreatedAt()
        );
    }
}
