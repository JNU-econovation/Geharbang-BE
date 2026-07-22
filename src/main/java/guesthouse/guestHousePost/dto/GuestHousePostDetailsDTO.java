package guesthouse.guestHousePost.dto;

import guesthouse.guestHousePost.domain.model.Amenity;
import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.domain.vo.Contact;
import guesthouse.guestHousePost.domain.vo.Mood;
import guesthouse.review.dto.response.ReviewSummaryResponse;
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
        List<String> imageUrls,
        Contact contact,
        String ownerMessage,
        Boolean isWished,
        Boolean isMine,
        List<Amenity> amenities,
        List<PartyWithImageUrlDTO> parties,
        List<RoomWithImageUrlDTO> rooms,
        ReviewSummaryResponse reviewSummary
){

    public static GuestHousePostDetailsDTO from(GuestHousePost guestHousePost, List<String> imageUrls, List<Amenity> amenities,
                                                List<PartyWithImageUrlDTO> parties, List<RoomWithImageUrlDTO> rooms,
                                                Boolean isWished, Boolean isMine,
                                                ReviewSummaryResponse reviewSummary) {
        return GuestHousePostDetailsDTO.builder()
                .guestHouseName(guestHousePost.getGuestHouseName())
                .region(guestHousePost.getRegion())
                .lotNumberAddress(guestHousePost.getLotNumberAddress())
                .roadNameAddress(guestHousePost.getRoadNameAddress())
                .coordinates(guestHousePost.getCoordinates())
                .introduction(guestHousePost.getIntroduction())
                .moods(guestHousePost.getMoods())
                .imageUrls(imageUrls)
                .contact(guestHousePost.getContact())
                .ownerMessage(guestHousePost.getOwnerMessage())
                .isWished(isWished)
                .isMine(isMine)
                .amenities(amenities)
                .parties(parties)
                .rooms(rooms)
                .reviewSummary(reviewSummary)
                .build();
    }
}
