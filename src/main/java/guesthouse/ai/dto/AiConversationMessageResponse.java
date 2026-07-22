package guesthouse.ai.dto;

import guesthouse.ai.domain.AiConversationMessage;

import java.time.LocalDateTime;

public record AiConversationMessageResponse(
        Long id,
        String role,
        String content,
        String imageUrl,
        String domain,
        Double confidence,
        LocalDateTime createdAt
) {
    public static AiConversationMessageResponse from(AiConversationMessage message) {
        return new AiConversationMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getImageUrl(),
                message.getDomain(),
                message.getConfidence(),
                message.getCreatedAt()
        );
    }
}
