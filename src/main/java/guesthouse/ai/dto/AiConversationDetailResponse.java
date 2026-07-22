package guesthouse.ai.dto;

import java.util.List;

public record AiConversationDetailResponse(
        String sessionId,
        String title,
        List<AiConversationMessageResponse> messages
) {
}
