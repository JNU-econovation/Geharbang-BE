package guesthouse.ai.dto;

import java.util.Map;

public record AiChatGatewayResponse(
        String sessionId,
        String answer,
        String domain,
        double confidence,
        Map<String, Object> context
) {
    public AiChatResponse toPublicResponse() {
        return new AiChatResponse(sessionId, answer, domain, confidence);
    }
}
