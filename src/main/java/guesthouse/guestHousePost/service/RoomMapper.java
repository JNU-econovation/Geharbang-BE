package guesthouse.guestHousePost.service;

import guesthouse.guestHousePost.domain.model.Room;
import guesthouse.guestHousePost.dto.request.GuestHouseCreateRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RoomMapper {

    public static Room toRoom(GuestHouseCreateRequest.Room room, Long guestHousePostId) {
        return Room.builder()
                .guestHousePostId(guestHousePostId)
                .name(room.name())
                .type(room.type())
                .headCount(room.headCountType())
                .checkInTime(room.checkInTime())
                .checkOutTime(room.checkInTime())
                .pricePerNight(room.pricePerNight())
                .build();
    }
}
