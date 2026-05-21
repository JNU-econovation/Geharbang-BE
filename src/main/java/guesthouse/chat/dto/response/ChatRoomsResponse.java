package guesthouse.chat.dto.response;

import java.util.List;

public record ChatRoomsResponse(
        List<ChatRoomDto> chatRooms
) {
}
