package guesthouse.ai.dto;

public record AiChatResponse(
        String sessionId,
        String answer,
        String domain,
        double confidence
) {
}
