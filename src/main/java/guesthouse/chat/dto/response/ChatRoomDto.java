package guesthouse.chat.dto.response;

import guesthouse.chat.domain.model.ChatRoom;

import java.time.LocalDateTime;

public record ChatRoomDto(
        Long id,
        Long applicationRecordId,
        Long staffRecruitmentId,
        String staffRecruitmentTitle,
        Long opponentId,
        String opponentName,
        String opponentImageUrl,
        String lastMessage,
        LocalDateTime lastMessageAt,
        Long unreadCount
) {
    public static ChatRoomDto of(
            ChatRoom room,
            Long userId,
            String opponentName,
            String opponentImageUrl,
            Long unreadCount
    ) {
        return new ChatRoomDto(
                room.getId(),
                room.getApplicationRecordId(),
                room.getStaffRecruitmentId(),
                room.getStaffRecruitmentTitle(),
                room.getOpponentId(userId),
                opponentName,
                opponentImageUrl,
                room.getLastMessage(),
                room.getLastMessageAt(),
                unreadCount
        );
    }
}
