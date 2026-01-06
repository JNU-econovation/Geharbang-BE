package guesthouse.guestHousePost.dto;

import guesthouse.guestHousePost.domain.model.Room;
import guesthouse.guestHousePost.domain.vo.RoomHeadCount;
import guesthouse.guestHousePost.domain.vo.RoomType;
import lombok.Builder;

import java.time.LocalTime;
import java.util.List;

@Builder
public record RoomWithImageUrlDTO(
        Long guestHousePostId,
        String name,
        RoomType type,
        RoomHeadCount headCount,
        LocalTime checkInTime,
        LocalTime checkOutTime,
        Integer pricePerNight,
        List<String> imageUrls
) {
    public static RoomWithImageUrlDTO from(Room room, List<String> imageUrls) {
        return RoomWithImageUrlDTO.builder()
                .guestHousePostId(room.getGuestHousePostId())
                .name(room.getName())
                .type(room.getType())
                .headCount(room.getHeadCount())
                .checkInTime(room.getCheckInTime())
                .checkOutTime(room.getCheckOutTime())
                .pricePerNight(room.getPricePerNight())
                .imageUrls(imageUrls)
                .build();
    }
}
