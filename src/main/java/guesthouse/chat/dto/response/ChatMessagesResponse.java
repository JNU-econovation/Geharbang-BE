package guesthouse.chat.dto.response;

import java.util.List;

public record ChatMessagesResponse(
        List<ChatMessageDto> messages
) {
}
