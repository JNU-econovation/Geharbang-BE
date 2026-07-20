package guesthouse.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @NotBlank
        @Size(max = 1000)
        String message,
        @Size(max = 100)
        String sessionId
) {
}
