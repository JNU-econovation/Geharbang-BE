package guesthouse.ai.dto;

import guesthouse.ai.domain.AiConversation;

import java.time.LocalDateTime;

public record AiConversationSummary(
        String sessionId,
        String title,
        String lastMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AiConversationSummary from(AiConversation conversation) {
        return new AiConversationSummary(
                conversation.getSessionId(),
                conversation.getTitle(),
                conversation.getLastMessage(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
