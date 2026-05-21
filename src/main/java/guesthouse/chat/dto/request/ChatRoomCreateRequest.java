package guesthouse.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatRoomCreateRequest(
        @NotNull
        Long applicationRecordId
) {
}
