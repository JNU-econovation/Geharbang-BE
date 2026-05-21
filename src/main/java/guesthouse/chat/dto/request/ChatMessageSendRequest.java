package guesthouse.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record ChatMessageSendRequest(
        @NotBlank
        @Length(max = 1000)
        String content
) {
}
