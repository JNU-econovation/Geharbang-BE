package guesthouse.chat.dto.request;

public record ChatRoomCreateRequest(
        Long applicationRecordId,
        Long staffRecruitmentId,
        Long guestHousePostId
) {
}
