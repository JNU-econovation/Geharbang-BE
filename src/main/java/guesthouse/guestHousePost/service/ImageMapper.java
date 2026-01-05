package guesthouse.guestHousePost.service;

import guesthouse.guestHousePost.domain.model.GuestHousePostImage;
import guesthouse.guestHousePost.domain.model.PartyImage;
import guesthouse.guestHousePost.domain.model.RoomImage;
import guesthouse.guestHousePost.dto.request.GuestHouseCreateRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.IntStream;

@UtilityClass
public class ImageMapper {

    public static List<GuestHousePostImage> toGuestHousePostImages(List<String> imageUrls, Long guestHousePostId) {
        return IntStream.range(0, imageUrls.size())
                .mapToObj(index -> createGuestHousePostImage(imageUrls.get(index), guestHousePostId, index))
                .toList();
    }

    private static GuestHousePostImage createGuestHousePostImage(String imageUrl, Long guestHousePostId, int index) {
        return GuestHousePostImage.builder()
                .guestHousePostId(guestHousePostId)
                .imageUrl(imageUrl)
                .index(index)
                .build();
    }

    public static List<PartyImage> toPartyImage(List<String> imageUrls, Long partyId) {
        return IntStream.range(0, imageUrls.size())
                .mapToObj(index -> createPartyImage(imageUrls.get(index), partyId, index))
                .toList();
    }

    private static PartyImage createPartyImage(String imageUrl, Long partyId, int index) {
        return PartyImage.builder()
                .partyId(partyId)
                .imageUrl(imageUrl)
                .index(index)
                .build();
    }

    public static List<RoomImage> toRoomImage(List<String> imageUrls, Long roomId) {
        return IntStream.range(0, imageUrls.size())
                .mapToObj(index -> createRoomImage(imageUrls.get(index), roomId, index))
                .toList();
    }

    private static RoomImage createRoomImage(String imageUrl, Long roomId, int index) {
        return RoomImage.builder()
                .roomId(roomId)
                .imageUrl(imageUrl)
                .index(index)
                .build();
    }
}
