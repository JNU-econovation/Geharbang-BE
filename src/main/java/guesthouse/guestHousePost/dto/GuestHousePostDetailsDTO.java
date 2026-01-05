package guesthouse.guestHousePost.dto;

import guesthouse.guestHousePost.domain.model.Amenity;
import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.domain.vo.Contact;
import guesthouse.guestHousePost.domain.vo.Mood;
import guesthouse.staffrecruitment.domain.vo.Region;
import lombok.Builder;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.Set;

@Builder
public record GuestHousePostDetailsDTO (
        String guestHouseName,
        Region region,
        String lotNumberAddress,
        String roadNameAddress,
        Point coordinates,
        String introduction,
        Set<Mood> moods,
        Contact contact,
        String ownerMessage,
        List<Amenity> amenities,
        List<PartyWithImageUrlDTO> parties,
        List<RoomWithImageUrlDTO> rooms
){

    public static GuestHousePostDetailsDTO from(GuestHousePost guestHousePost, List<String> images, List<Amenity> amenities,
                                                List<PartyWithImageUrlDTO> parties, List<RoomWithImageUrlDTO> rooms) {
        return GuestHousePostDetailsDTO.builder()
                .guestHouseName(guestHousePost.getGuestHouseName())
                .region(guestHousePost.getRegion())
                .lotNumberAddress(guestHousePost.getLotNumberAddress())
                .roadNameAddress(guestHousePost.getRoadNameAddress())
                .coordinates(guestHousePost.getCoordinates())
                .introduction(guestHousePost.getIntroduction())
                .moods(guestHousePost.getMoods())
                .contact(guestHousePost.getContact())
                .ownerMessage(guestHousePost.getOwnerMessage())
                .amenities(amenities)
                .parties(parties)
                .rooms(rooms)
                .build();
    }
}
