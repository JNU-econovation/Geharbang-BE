package guesthouse.ai.dto;

import java.util.Map;

public record AiChatGatewayRequest(
        String message,
        String sessionId,
        Map<String, Object> context
) {
}
